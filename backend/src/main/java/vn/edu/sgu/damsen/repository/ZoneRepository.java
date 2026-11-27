package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.damsen.domain.Zone;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByActiveTrueOrderByCode();
    boolean existsByCode(String code);
}
