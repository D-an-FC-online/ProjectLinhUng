package vn.edu.sgu.damsen.provider;

/** Cổng tới dịch vụ chuyển văn bản thành giọng nói. Trả về dữ liệu MP3. */
public interface TtsProvider {
    boolean isEnabled();

    byte[] synthesizeSsml(String ssml, String lang);
}
