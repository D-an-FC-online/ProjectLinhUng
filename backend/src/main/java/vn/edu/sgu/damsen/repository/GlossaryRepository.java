package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.damsen.domain.GlossaryTerm;

public interface GlossaryRepository extends JpaRepository<GlossaryTerm, Long> {
    List<GlossaryTerm> findAllByOrderByTerm();
    boolean existsByTerm(String term);
}
