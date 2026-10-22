package com.example.entity;

/**
 * Danh sách ngôn ngữ thuyết minh được hỗ trợ.
 * Lưu vào DB dưới dạng chuỗi (VI, EN, ...) nhờ @Enumerated(EnumType.STRING).
 * Muốn thêm ngôn ngữ mới: thêm một giá trị vào đây, không cần sửa cấu trúc bảng.
 */
public enum LanguageCode {
    VI, // Tiếng Việt
    EN, // Tiếng Anh
    KO, // Tiếng Hàn
    ZH, // Tiếng Trung
    JA  // Tiếng Nhật
}