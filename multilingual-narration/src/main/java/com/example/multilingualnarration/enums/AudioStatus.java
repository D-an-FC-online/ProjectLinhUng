package com.example.multilingualnarration.enums;

/**
 * Trạng thái âm thanh của một bản thuyết minh, suy ra từ cột {@code audio_url}
 * (có URL => AVAILABLE, null => NOT_AVAILABLE). Không lưu riêng trong database.
 *
 * Khi {@code NOT_AVAILABLE}, module Text-to-Speech biết cần tạo file âm thanh.
 */
public enum AudioStatus {
    AVAILABLE,
    NOT_AVAILABLE
}
