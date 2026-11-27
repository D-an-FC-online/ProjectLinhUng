package vn.edu.sgu.damsen.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processing_jobs")
public class ProcessingJob {
    public static final String TRANSLATE = "TRANSLATE";
    public static final String TTS = "TTS";
    public static final String QUEUED = "QUEUED";
    public static final String RUNNING = "RUNNING";
    public static final String SUCCEEDED = "SUCCEEDED";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "job_type") private String jobType;
    private String status = QUEUED;
    @Column(name = "target_version_id") private Long targetVersionId;
    @Column(name = "target_language") private String targetLanguage;
    @Column(name = "result_version_id") private Long resultVersionId;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "started_at") private LocalDateTime startedAt;
    @Column(name = "finished_at") private LocalDateTime finishedAt;
    @Column(name = "error_message", columnDefinition = "NVARCHAR(MAX)") private String errorMessage;
    @Column(name = "retry_count") private int retryCount;

    public boolean isFinished() {
        return SUCCEEDED.equals(status) || FAILED.equals(status) || CANCELLED.equals(status);
    }

    public Long getId() { return id; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getTargetVersionId() { return targetVersionId; }
    public void setTargetVersionId(Long v) { this.targetVersionId = v; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public Long getResultVersionId() { return resultVersionId; }
    public void setResultVersionId(Long resultVersionId) { this.resultVersionId = resultVersionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}
