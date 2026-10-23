package com.example.multilingualnarration.enums;

/**
 * Trạng thái vòng đời của một bản thuyết minh (lưu trong cột {@code narrations.status}).
 *
 * <ul>
 *   <li>{@link #DRAFT}: đang soạn hoặc chờ duyệt (ví dụ bản dịch máy chưa được người kiểm tra).
 *       KHÔNG hiển thị cho khách tham quan.</li>
 *   <li>{@link #PUBLISHED}: đã duyệt, sẵn sàng phục vụ khách tham quan.</li>
 * </ul>
 *
 * Lưu dạng chuỗi (EnumType.STRING) để thêm trạng thái mới không làm sai lệch dữ liệu cũ.
 */
public enum NarrationStatus {
    DRAFT,
    PUBLISHED
}
