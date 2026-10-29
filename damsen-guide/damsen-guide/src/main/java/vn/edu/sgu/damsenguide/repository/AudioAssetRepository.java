package vn.edu.sgu.damsenguide.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.sgu.damsenguide.entity.AudioAsset;
import java.util.Optional;

public interface AudioAssetRepository extends JpaRepository<AudioAsset, Long> {
    // Chỉ phục vụ file audio đang gắn với một điểm ACTIVE (BR23, BR32)
    @Query("""
        SELECT a FROM AudioAsset a
        WHERE a.id = :assetId AND a.status = 'READY'
        AND EXISTS (
            SELECT 1 FROM PoiContent pc JOIN pc.poi p
            WHERE pc.audioAsset = a AND p.status = 'ACTIVE'
        )
    """)
    Optional<AudioAsset> findActivePublishedAsset(Long assetId);
}
