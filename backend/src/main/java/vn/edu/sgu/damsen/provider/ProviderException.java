package vn.edu.sgu.damsen.provider;

/** Lỗi khi gọi dịch vụ ngoài (dịch máy / TTS), hoặc dịch vụ chưa được cấu hình. */
public class ProviderException extends RuntimeException {
    public ProviderException(String message) { super(message); }
    public ProviderException(String message, Throwable cause) { super(message, cause); }
}
