package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.sgu.damsen.domain.Poi;

public interface PoiRepository extends JpaRepository<Poi, Long> {
    boolean existsByCode(String code);

    /** Điểm đang hoạt động thuộc khu vực đang bật. */
    @Query("select p from Poi p, Zone z where z.id = p.zoneId and z.active = true and p.status = 'ACTIVE' order by p.code")
    List<Poi> findVisible();

    List<Poi> findAllByOrderByCode();
}
