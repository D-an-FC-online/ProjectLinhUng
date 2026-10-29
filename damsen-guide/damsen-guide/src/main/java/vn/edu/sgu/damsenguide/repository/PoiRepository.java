package vn.edu.sgu.damsenguide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.sgu.damsenguide.entity.Poi;
import java.util.List;

public interface PoiRepository extends JpaRepository<Poi, Long> {
    // Chỉ lấy các điểm ACTIVE và đã có nội dung nguồn tiếng Việt (BR27)
    @Query("""
        SELECT DISTINCT p FROM Poi p
        LEFT JOIN FETCH p.contents c
        LEFT JOIN FETCH c.audioAsset a
        WHERE p.status = 'ACTIVE'
        AND EXISTS (SELECT 1 FROM PoiContent pc WHERE pc.poi = p AND pc.languageCode = 'vi')
        ORDER BY p.sortOrder ASC, p.code ASC
    """)
    List<Poi> findAllActiveWithContents();
}
