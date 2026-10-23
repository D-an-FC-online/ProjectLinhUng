package com.example.multilingualnarration.repository;

import com.example.multilingualnarration.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer cho bảng {@code languages}.
 */
public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);

    /** Danh sách ngôn ngữ đang được kích hoạt, sắp theo thứ tự thêm vào. */
    List<Language> findByActiveTrueOrderByIdAsc();

    /** Toàn bộ ngôn ngữ (kể cả đã tắt) — dùng cho màn hình quản trị. */
    List<Language> findAllByOrderByIdAsc();
}
