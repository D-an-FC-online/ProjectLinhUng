package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audio_assets")
public class AudioAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "narration_version_id") private Long narrationVersionId;
    @Column(name = "storage_key") private String storageKey;
    private String status = "PENDING";
    private String source = "UPLOAD";
    @Column(name = "text_hash", columnDefinition = "CHAR(64)") private String textHash;
    @Column(name = "content_hash", columnDefinition = "CHAR(64)") private String contentHash;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;

    public boolean isReady() { return "READY".equals(status); }

    public Long getId() { return id; }
    public Long getNarrationVersionId() { return narrationVersionId; }
    public void setNarrationVersionId(Long v) { this.narrationVersionId = v; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTextHash() { return textHash; }
    public void setTextHash(String textHash) { this.textHash = textHash; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
