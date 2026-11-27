package vn.edu.sgu.damsen.provider;

/** Cổng tới dịch vụ dịch máy bên ngoài. Đầu vào/ra là HTML (để giữ thẻ notranslate của thuật ngữ). */
public interface TranslationProvider {
    boolean isEnabled();

    String translateHtml(String html, String sourceLang, String targetLang);
}
