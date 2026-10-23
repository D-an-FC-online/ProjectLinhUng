package com.example.multilingualnarration.enums;

/**
 * Kết quả tra cứu nội dung thuyết minh theo (địa điểm, ngôn ngữ) — KHÔNG lưu trong database,
 * được tính ở tầng Service để Frontend và các module khác biết phải làm gì tiếp theo.
 *
 * <ul>
 *   <li>{@link #AVAILABLE}: có bản PUBLISHED => hiển thị nội dung cho khách.</li>
 *   <li>{@link #PENDING}: chỉ có bản DRAFT (đang soạn / chờ duyệt) => khách chưa xem được,
 *       module dịch KHÔNG cần tạo thêm.</li>
 *   <li>{@link #NOT_AVAILABLE}: chưa có bản nào => module dịch thuật có thể tạo mới.</li>
 * </ul>
 */
public enum NarrationAvailability {
    AVAILABLE,
    PENDING,
    NOT_AVAILABLE
}
