package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/** Con trỏ xuất bản: mỗi (ngôn ngữ, POI) có tối đa một phiên bản đang phục vụ khách. */
@Entity
@Table(name = "publications")
@IdClass(Publication.Key.class)
public class Publication {
    @Id @Column(name = "language_code") private String languageCode;
    @Id @Column(name = "poi_id") private Long poiId;
    @Column(name = "narration_version_id") private Long narrationVersionId;
    @Column(name = "audio_asset_id") private Long audioAssetId;
    @Column(name = "published_at") private LocalDateTime publishedAt;

    public Publication() {}

    public Publication(String languageCode, Long poiId) {
        this.languageCode = languageCode;
        this.poiId = poiId;
    }

    public String getLanguageCode() { return languageCode; }
    public Long getPoiId() { return poiId; }
    public Long getNarrationVersionId() { return narrationVersionId; }
    public void setNarrationVersionId(Long v) { this.narrationVersionId = v; }
    public Long getAudioAssetId() { return audioAssetId; }
    public void setAudioAssetId(Long audioAssetId) { this.audioAssetId = audioAssetId; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public static class Key implements Serializable {
        private String languageCode;
        private Long poiId;

        public Key() {}
        public Key(String languageCode, Long poiId) { this.languageCode = languageCode; this.poiId = poiId; }

        @Override public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(languageCode, k.languageCode) && Objects.equals(poiId, k.poiId);
        }
        @Override public int hashCode() { return Objects.hash(languageCode, poiId); }
    }
}
