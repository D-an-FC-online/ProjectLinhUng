package vn.edu.sgu.damsen.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import vn.edu.sgu.damsen.domain.GlossaryTerm;
import vn.edu.sgu.damsen.domain.NarrationVersion;

/** Kiểm thử đơn vị không cần CSDL (chạy bằng: mvn test). */
class ServiceUnitTest {

    private static GlossaryTerm term(String t, boolean keep, String en, String aliasKo) {
        GlossaryTerm g = new GlossaryTerm();
        g.setTerm(t);
        g.setTermType("PLACE");
        g.setKeepOriginal(keep);
        g.setTranslationEn(en);
        g.setTtsAliasKo(aliasKo);
        return g;
    }

    @Test
    void contentHashLaSha256CuaUtf8() {
        // giá trị kiểm chứng: sha256("A\nB")
        assertEquals("23519a43c66b4c342f25b32e09797ec5f3fc0be388cd8243fb3449afbdce4013", HashUtil.contentHash("A", "B"));
        assertEquals(HashUtil.contentHash("Đầm", "Sen"), HashUtil.contentHash("Đầm", "Sen"));
        assertNotEquals(HashUtil.contentHash("a", "b"), HashUtil.contentHash("a", "c"));
    }

    @Test
    void haversineKhoangCach() {
        // 0.001 độ vĩ ~ 111 m
        double d = GeoUtil.distanceM(10.7636, 106.6332, 10.7646, 106.6332);
        assertEquals(111, d, 1.5);
    }

    @Test
    void thuatNguDuocBaoVeKhiDich() {
        List<GlossaryTerm> terms = List.of(term("Đầm Sen", true, "Dam Sen", null));
        String html = TranslationService.protect("Công viên Đầm Sen <đẹp>", terms, "en");
        assertTrue(html.contains("<span class=\"notranslate\">Dam Sen</span>"));
        assertTrue(html.contains("&lt;đẹp&gt;"));
        // giả lập dịch vụ dịch trả về HTML
        String back = TranslationService.unprotect("<span class=\"notranslate\">Dam Sen</span> park &lt;nice&gt;<br>line 2");
        assertEquals("Dam Sen park <nice>\nline 2", back);
    }

    @Test
    void ssmlCoCachDoc() {
        NarrationVersion v = new NarrationVersion();
        v.setLanguageCode("ko");
        v.setTitle("담센");
        v.setBody("담센 & 수족관");
        GlossaryTerm g = term("Đầm Sen", true, "Dam Sen", "담 센");
        g.setTranslationKo("담센");
        String ssml = TtsService.buildSsml(v, List.of(g));
        assertTrue(ssml.startsWith("<speak>"));
        assertTrue(ssml.contains("<sub alias=\"담 센\">담센</sub> &amp; 수족관"));
    }

    @Test
    void timKiemKhongDau() {
        assertEquals("thuy cung dam sen", PublicContentService.fold("Thủy cung Đầm Sen"));
        assertEquals("vi", PublicContentService.normalizeLang(null));
        assertEquals("en", PublicContentService.normalizeLang("en-US"));
    }
}
