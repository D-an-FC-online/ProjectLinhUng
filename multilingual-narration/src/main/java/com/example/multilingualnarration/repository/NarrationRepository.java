package com.example.multilingualnarration.repository;

import com.example.multilingualnarration.entity.Narration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Layer cho bảng {@code narrations}.
 *
 * <p><b>Chống N+1:</b> Narration có 2 quan hệ LAZY (location, language). Nếu chỉ dùng findById
 * rồi gọi narration.getLanguage().getCode(), Hibernate sẽ bắn thêm câu SELECT cho mỗi quan hệ.
 * {@code @EntityGraph} và {@code join fetch} yêu cầu Hibernate JOIN và lấy tất cả trong 1 câu SQL.
 */
public interface NarrationRepository extends JpaRepository<Narration, Long> {

    /** Lấy narration kèm location + language trong 1 câu SQL. ("WithDetails" chỉ là tên gợi nhớ.) */
    @EntityGraph(attributePaths = {"location", "language"})
    Optional<Narration> findWithDetailsById(Long id);

    boolean existsByLocationIdAndLanguageId(Long locationId, Long languageId);

    boolean existsByLocationId(Long locationId);

    /**
     * Tất cả bản thuyết minh của một địa điểm thuộc các ngôn ngữ đang kích hoạt.
     * {@code join fetch} nạp luôn Language trong cùng câu truy vấn.
     */
    @Query("""
            select n from Narration n
            join fetch n.language l
            where n.location.id = :locationId
              and l.active = true
            order by l.id
            """)
    List<Narration> findAllWithActiveLanguageByLocationId(@Param("locationId") Long locationId);
}
