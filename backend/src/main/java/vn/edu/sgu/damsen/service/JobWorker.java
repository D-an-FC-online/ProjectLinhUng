package vn.edu.sgu.damsen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.sgu.damsen.domain.NarrationVersion;
import vn.edu.sgu.damsen.domain.ProcessingJob;
import vn.edu.sgu.damsen.repository.NarrationVersionRepository;

/**
 * Job Worker (Scheduled) trong Component Diagram: định kỳ lấy các tác vụ QUEUED và chạy.
 * Gọi dịch vụ ngoài NGOÀI transaction để không giữ khóa CSDL trong lúc chờ mạng.
 */
@Component
public class JobWorker {
    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);

    private final JobService jobs;
    private final TranslationService translation;
    private final TtsService tts;
    private final NarrationVersionRepository versions;

    public JobWorker(JobService jobs, TranslationService translation, TtsService tts,
                     NarrationVersionRepository versions) {
        this.jobs = jobs;
        this.translation = translation;
        this.tts = tts;
        this.versions = versions;
    }

    @Scheduled(fixedDelayString = "${app.jobs.poll-interval-ms:3000}", initialDelay = 5000)
    public void poll() {
        for (Long id : jobs.queuedIds()) {
            if (!jobs.markRunning(id)) continue;
            try {
                ProcessingJob job = jobs.snapshot(id);
                NarrationVersion target = versions.findById(job.getTargetVersionId()).orElseThrow();
                if (ProcessingJob.TRANSLATE.equals(job.getJobType())) {
                    jobs.completeTranslate(id, translation.translate(target, job.getTargetLanguage()));
                } else {
                    jobs.completeTts(id, tts.synthesize(target));
                }
            } catch (Exception e) {
                log.warn("Tác vụ {} thất bại: {}", id, e.getMessage());
                jobs.markFailed(id, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            }
        }
    }
}
