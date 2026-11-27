package vn.edu.sgu.damsen.service;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import vn.edu.sgu.damsen.domain.GlossaryTerm;
import vn.edu.sgu.damsen.domain.Language;
import vn.edu.sgu.damsen.domain.NarrationVersion;
import vn.edu.sgu.damsen.provider.TranslationProvider;
import vn.edu.sgu.damsen.repository.GlossaryRepository;

/**
 * Dịch máy có áp dụng bảng thuật ngữ (UC09): trước khi gửi đi, mỗi thuật ngữ được thay bằng
 * <span class="notranslate">cách viết đích</span> để dịch vụ không dịch sai; sau đó gỡ thẻ ra.
 */
@Service
public class TranslationService {
    private static final Pattern NOTRANSLATE =
            Pattern.compile("<span class=\"notranslate\"[^>]*>(.*?)</span>", Pattern.DOTALL);

    private final TranslationProvider provider;
    private final GlossaryRepository glossary;

    public TranslationService(TranslationProvider provider, GlossaryRepository glossary) {
        this.provider = provider;
        this.glossary = glossary;
    }

    public boolean isEnabled() { return provider.isEnabled(); }

    public record Translated(String title, String body) {}

    public Translated translate(NarrationVersion source, String targetLang) {
        List<GlossaryTerm> terms = glossary.findAll().stream()
                .filter(g -> g.isKeepOriginal() || g.getTranslationEn() != null || g.getTranslationKo() != null)
                .sorted(Comparator.comparingInt((GlossaryTerm g) -> g.getTerm().length()).reversed())
                .toList();
        String title = unprotect(provider.translateHtml(protect(source.getTitle(), terms, targetLang), Language.SOURCE, targetLang));
        String body = unprotect(provider.translateHtml(protect(source.getBody(), terms, targetLang), Language.SOURCE, targetLang));
        return new Translated(title, body);
    }

    static String protect(String text, List<GlossaryTerm> terms, String targetLang) {
        String html = HtmlUtils.htmlEscape(text, "UTF-8");
        for (GlossaryTerm g : terms) {
            String escapedTerm = HtmlUtils.htmlEscape(g.getTerm(), "UTF-8");
            String replacement = "<span class=\"notranslate\">"
                    + HtmlUtils.htmlEscape(g.translationFor(targetLang), "UTF-8") + "</span>";
            html = html.replace(escapedTerm, replacement);
        }
        return html.replace("\n", "<br>");
    }

    static String unprotect(String html) {
        Matcher m = NOTRANSLATE.matcher(html);
        String noSpans = m.replaceAll(r -> Matcher.quoteReplacement(r.group(1)));
        return HtmlUtils.htmlUnescape(noSpans.replaceAll("(?i)<br\\s*/?>", "\n")).trim();
    }
}
