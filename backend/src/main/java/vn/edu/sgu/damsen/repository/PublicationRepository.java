package vn.edu.sgu.damsen.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.damsen.domain.Publication;

public interface PublicationRepository extends JpaRepository<Publication, Publication.Key> {
    Optional<Publication> findByLanguageCodeAndPoiId(String languageCode, Long poiId);
    List<Publication> findByPoiId(Long poiId);
    List<Publication> findByLanguageCodeIn(List<String> languageCodes);
    Optional<Publication> findByNarrationVersionId(Long narrationVersionId);
    boolean existsByAudioAssetId(Long audioAssetId);
}
