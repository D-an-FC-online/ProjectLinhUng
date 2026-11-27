package vn.edu.sgu.damsen.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.damsen.config.AppProperties;
import vn.edu.sgu.damsen.domain.*;
import vn.edu.sgu.damsen.repository.*;
import vn.edu.sgu.damsen.web.dto.Dtos.JobDto;

/** Tạo, theo dõi, thử lại và hủy tác vụ nền dịch/TTS (UC09, UC11, UC13). */
@Service
@Transactional
public class JobService {
    private static final List<String> ACTIVE = List.of(ProcessingJob.QUEUED, ProcessingJob.RUNNING);

    private final ProcessingJobRepository jobs;
    private final NarrationVersionRepository versions;
    private final PoiRepository pois;
    private final NarrationService narration;
    private final AppProperties props;

    public JobService(ProcessingJobRepository jobs, NarrationVersionRepository versions, PoiRepository pois,
                      NarrationService narration, AppProperties props) {
        this.jobs = jobs;
        this.versions = versions;
        this.pois = pois;
        this.narration = narration;
        this.props = props;
    }

    /** Yêu cầu dịch tự động bản tiếng Việt đã duyệt sang en/ko. Kết quả là một bản nháp mới. */
    public JobDto requestTranslate(Long sourceVersionId, String targetLang) {
        NarrationVersion src = narration.requireVersion(sourceVersionId);
        if (!Language.SOURCE.equals(src.getLanguageCode())) throw ApiException.badRequest("Chỉ dịch từ bản tiếng Việt");
        if (!src.isApproved()) throw ApiException.conflict("Hãy duyệt bản tiếng Việt trước khi dịch");
        if (jobs.existsByTargetVersionIdAndJobTypeAndTargetLanguageAndStatusIn(
                sourceVersionId, ProcessingJob.TRANSLATE, targetLang, ACTIVE)) {
            throw ApiException.conflict("Đang có tác vụ dịch sang " + targetLang + " cho phiên bản này");
        }
        return toDto(create(ProcessingJob.TRANSLATE, sourceVersionId, targetLang));
    }

    /** Yêu cầu tạo giọng đọc cho một phiên bản đã duyệt. */
    public JobDto requestTts(Long versionId) {
        NarrationVersion v = narration.requireVersion(versionId);
        if (!v.isApproved()) throw ApiException.conflict("Chỉ tạo giọng đọc cho phiên bản đã duyệt");
        if (jobs.existsByTargetVersionIdAndJobTypeAndTargetLanguageAndStatusIn(
                versionId, ProcessingJob.TTS, v.getLanguageCode(), ACTIVE)) {
            throw ApiException.conflict("Đang có tác vụ tạo giọng đọc cho phiên bản này");
        }
        return toDto(create(ProcessingJob.TTS, versionId, v.getLanguageCode()));
    }

    private ProcessingJob create(String type, Long versionId, String lang) {
        ProcessingJob j = new ProcessingJob();
        j.setJobType(type);
        j.setTargetVersionId(versionId);
        j.setTargetLanguage(lang);
        j.setStatus(ProcessingJob.QUEUED);
        j.setCreatedAt(now());
        return jobs.save(j);
    }

    @Transactional(readOnly = true)
    public List<JobDto> search(String status, String type, Long poiId) {
        return jobs.search(emptyToNull(status), emptyToNull(type), poiId).stream().limit(200).map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public JobDto get(Long id) {
        return toDto(require(id));
    }

    public JobDto retry(Long id) {
        ProcessingJob j = require(id);
        if (!ProcessingJob.FAILED.equals(j.getStatus()) && !ProcessingJob.CANCELLED.equals(j.getStatus())) {
            throw ApiException.conflict("Chỉ thử lại được tác vụ FAILED hoặc CANCELLED");
        }
        if (j.getRetryCount() >= props.jobs().maxRetries()) {
            throw ApiException.conflict("Đã thử lại tối đa " + props.jobs().maxRetries() + " lần");
        }
        j.setRetryCount(j.getRetryCount() + 1);
        j.setStatus(ProcessingJob.QUEUED);
        j.setErrorMessage(null);
        j.setStartedAt(null);
        j.setFinishedAt(null);
        return toDto(j);
    }

    public JobDto cancel(Long id) {
        ProcessingJob j = require(id);
        if (j.isFinished()) throw ApiException.conflict("Tác vụ đã kết thúc");
        j.setStatus(ProcessingJob.CANCELLED);
        j.setFinishedAt(now());
        return toDto(j);
    }

    /* ---------- dùng bởi JobWorker ---------- */

    @Transactional(readOnly = true)
    public List<Long> queuedIds() {
        return jobs.findTop10ByStatusOrderByCreatedAtAsc(ProcessingJob.QUEUED).stream().map(ProcessingJob::getId).toList();
    }

    /** Chuyển QUEUED -> RUNNING. Trả về false nếu job đã bị hủy/đổi trạng thái. */
    public boolean markRunning(Long id) {
        ProcessingJob j = require(id);
        if (!ProcessingJob.QUEUED.equals(j.getStatus())) return false;
        j.setStatus(ProcessingJob.RUNNING);
        j.setStartedAt(now());
        return true;
    }

    public void markFailed(Long id, String message) {
        ProcessingJob j = require(id);
        if (ProcessingJob.CANCELLED.equals(j.getStatus())) return;
        j.setStatus(ProcessingJob.FAILED);
        j.setErrorMessage(message);
        j.setFinishedAt(now());
    }

    /** Ghi kết quả dịch thành bản nháp mới; bỏ qua nếu job đã bị hủy trong lúc chạy. */
    public void completeTranslate(Long id, TranslationService.Translated t) {
        ProcessingJob j = require(id);
        if (!ProcessingJob.RUNNING.equals(j.getStatus())) return;
        NarrationVersion src = narration.requireVersion(j.getTargetVersionId());
        NarrationVersion draft = narration.newVersion(src.getPoiId(), j.getTargetLanguage(), src.getId(), t.title(), t.body());
        j.setResultVersionId(draft.getId());
        j.setStatus(ProcessingJob.SUCCEEDED);
        j.setFinishedAt(now());
    }

    public void completeTts(Long id, byte[] mp3) {
        ProcessingJob j = require(id);
        if (!ProcessingJob.RUNNING.equals(j.getStatus())) return;
        NarrationVersion v = narration.requireVersion(j.getTargetVersionId());
        narration.saveAudio(v, mp3, "mp3", "TTS");
        j.setStatus(ProcessingJob.SUCCEEDED);
        j.setFinishedAt(now());
    }

    @Transactional(readOnly = true)
    public ProcessingJob snapshot(Long id) {
        return require(id);
    }

    /* ---------- helpers ---------- */

    private ProcessingJob require(Long id) {
        return jobs.findById(id).orElseThrow(() -> ApiException.notFound("tác vụ"));
    }

    private JobDto toDto(ProcessingJob j) {
        NarrationVersion v = versions.findById(j.getTargetVersionId()).orElse(null);
        Poi p = v == null ? null : pois.findById(v.getPoiId()).orElse(null);
        return new JobDto(j.getId(), j.getJobType(), j.getStatus(), j.getTargetVersionId(), j.getTargetLanguage(),
                j.getResultVersionId(), p == null ? null : p.getId(), p == null ? null : p.getCode(),
                v == null ? null : v.getLanguageCode(), j.getCreatedAt(), j.getStartedAt(), j.getFinishedAt(),
                j.getErrorMessage(), j.getRetryCount());
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC).withNano(0);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
