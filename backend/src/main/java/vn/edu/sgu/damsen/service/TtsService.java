package vn.edu.sgu.damsen.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import vn.edu.sgu.damsen.domain.GlossaryTerm;
import vn.edu.sgu.damsen.domain.NarrationVersion;
import vn.edu.sgu.damsen.provider.TtsProvider;
import vn.edu.sgu.damsen.repository.GlossaryRepository;

/** Tạo giọng đọc (UC11): dựng SSML, thay thuật ngữ bằng <sub alias="cách đọc"> theo bảng thuật ngữ. */
@Service
public class TtsService {
    private final TtsProvider provider;
    private final GlossaryRepository glossary;

    public TtsService(TtsProvider provider, GlossaryRepository glossary) {
        this.provider = provider;
        this.glossary = glossary;
    }

    public boolean isEnabled() { return provider.isEnabled(); }

    public byte[] synthesize(NarrationVersion v) {
        return provider.synthesizeSsml(buildSsml(v, glossary.findAll()), v.getLanguageCode());
    }

    static String buildSsml(NarrationVersion v, List<GlossaryTerm> terms) {
        String lang = v.getLanguageCode();
        String title = xml(v.getTitle());
        String body = xml(v.getBody());
        List<GlossaryTerm> sorted = terms.stream()
                .filter(g -> g.ttsAliasFor(lang) != null)
                .sorted(Comparator.comparingInt((GlossaryTerm g) -> g.getTerm().length()).reversed())
                .toList();
        for (GlossaryTerm g : sorted) {
            String written = xml(g.translationFor(lang));
            String sub = "<sub alias=\"" + xml(g.ttsAliasFor(lang)) + "\">" + written + "</sub>";
            title = title.replace(written, sub);
            body = body.replace(written, sub);
        }
        return "<speak><p>" + title + "</p><break time=\"600ms\"/><p>" + body.replace("\n", "</p><p>") + "</p></speak>";
    }

    private static String xml(String s) {
        return HtmlUtils.htmlEscape(s == null ? "" : s, "UTF-8");
    }
}
