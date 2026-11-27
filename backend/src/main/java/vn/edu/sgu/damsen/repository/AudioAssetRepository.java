package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.damsen.domain.AudioAsset;

public interface AudioAssetRepository extends JpaRepository<AudioAsset, Long> {
    List<AudioAsset> findByNarrationVersionIdOrderByIdDesc(Long narrationVersionId);
    List<AudioAsset> findByNarrationVersionIdIn(List<Long> versionIds);
}
