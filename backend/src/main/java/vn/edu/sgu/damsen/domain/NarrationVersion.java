package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "narration_versions")
public class NarrationVersion {
    public static final String DRAFT = "DRAFT";
    public static final String APPROVED = "APPROVED";
    public static final String ARCHIVED = "ARCHIVED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "poi_id") private Long poiId;
    @Column(name = "language_code") private String languageCode;
    @Column(name = "version_no") private int versionNo;
    @Column(name = "source_version_id") private Long sourceVersionId;
    private String title;
    @Column(columnDefinition = "NVARCHAR(MAX)") private String body;
    private String status = DRAFT;
    @Column(name = "content_hash", columnDefinition = "CHAR(64)") private String contentHash;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;

    public boolean isDraft() { return DRAFT.equals(status); }
    public boolean isApproved() { return APPROVED.equals(status); }

    public Long getId() { return id; }
    public Long getPoiId() { return poiId; }
    public void setPoiId(Long poiId) { this.poiId = poiId; }
    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) { this.languageCode = languageCode; }
    public int getVersionNo() { return versionNo; }
    public void setVersionNo(int versionNo) { this.versionNo = versionNo; }
    public Long getSourceVersionId() { return sourceVersionId; }
    public void setSourceVersionId(Long sourceVersionId) { this.sourceVersionId = sourceVersionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
