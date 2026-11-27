package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "glossary")
public class GlossaryTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String term;
    @Column(name = "term_type") private String termType;
    @Column(name = "keep_original") private boolean keepOriginal;
    @Column(name = "translation_en") private String translationEn;
    @Column(name = "translation_ko") private String translationKo;
    @Column(name = "tts_alias_vi") private String ttsAliasVi;
    @Column(name = "tts_alias_en") private String ttsAliasEn;
    @Column(name = "tts_alias_ko") private String ttsAliasKo;
    private String note;

    /** Cách viết thuật ngữ trong bản dịch sang ngôn ngữ lang. */
    public String translationFor(String lang) {
        String t = switch (lang) {
            case "en" -> translationEn;
            case "ko" -> translationKo;
            default -> term;
        };
        return (t == null || t.isBlank()) ? term : t;
    }

    /** Cách đọc (SSML sub alias) theo ngôn ngữ, null nếu không có. */
    public String ttsAliasFor(String lang) {
        String a = switch (lang) {
            case "vi" -> ttsAliasVi;
            case "en" -> ttsAliasEn;
            case "ko" -> ttsAliasKo;
            default -> null;
        };
        return (a == null || a.isBlank()) ? null : a;
    }

    public Long getId() { return id; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getTermType() { return termType; }
    public void setTermType(String termType) { this.termType = termType; }
    public boolean isKeepOriginal() { return keepOriginal; }
    public void setKeepOriginal(boolean keepOriginal) { this.keepOriginal = keepOriginal; }
    public String getTranslationEn() { return translationEn; }
    public void setTranslationEn(String v) { this.translationEn = v; }
    public String getTranslationKo() { return translationKo; }
    public void setTranslationKo(String v) { this.translationKo = v; }
    public String getTtsAliasVi() { return ttsAliasVi; }
    public void setTtsAliasVi(String v) { this.ttsAliasVi = v; }
    public String getTtsAliasEn() { return ttsAliasEn; }
    public void setTtsAliasEn(String v) { this.ttsAliasEn = v; }
    public String getTtsAliasKo() { return ttsAliasKo; }
    public void setTtsAliasKo(String v) { this.ttsAliasKo = v; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
