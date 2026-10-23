package com.example.multilingualnarration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Locale;

/**
 * Entity ánh xạ bảng {@code languages} — ngôn ngữ được hệ thống hỗ trợ.
 *
 * <p>Ngôn ngữ là DỮ LIỆU, không phải hằng số trong code: thêm tiếng Pháp chỉ cần thêm 1 dòng
 * vào bảng này (qua API quản trị hoặc một migration mới), không phải sửa/biên dịch lại code.
 *
 * <p>{@code code} là mã ngôn ngữ theo chuẩn ISO 639-1 / BCP 47 (vi, en, ja, ko, zh...),
 * luôn được lưu dạng chữ thường và là duy nhất (unique constraint uk_languages_code).
 */
@Entity
@Table(name = "languages")
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    /** Tên ngôn ngữ bằng tiếng Việt, ví dụ "Tiếng Nhật". */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Tên ngôn ngữ viết bằng chính ngôn ngữ đó, ví dụ "日本語" — dùng cho màn hình chọn ngôn ngữ. */
    @Column(name = "native_name", length = 100)
    private String nativeName;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Language() {
    }

    /**
     * Chuẩn hóa mã ngôn ngữ: bỏ khoảng trắng 2 đầu và chuyển về chữ thường.
     * Nhờ vậy "EN", " en " và "en" được coi là cùng một ngôn ngữ.
     */
    public static String normalizeCode(String code) {
        return code == null ? null : code.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
