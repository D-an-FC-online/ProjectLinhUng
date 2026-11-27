package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.sgu.damsen.domain.NarrationVersion;

public interface NarrationVersionRepository extends JpaRepository<NarrationVersion, Long> {
    List<NarrationVersion> findByPoiIdOrderByLanguageCodeAscVersionNoDesc(Long poiId);

    @Query("select coalesce(max(v.versionNo), 0) from NarrationVersion v where v.poiId = ?1 and v.languageCode = ?2")
    int maxVersionNo(Long poiId, String languageCode);
}
