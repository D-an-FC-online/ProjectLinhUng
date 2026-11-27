package vn.edu.sgu.damsen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.sgu.damsen.domain.ProcessingJob;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, Long> {
    List<ProcessingJob> findTop10ByStatusOrderByCreatedAtAsc(String status);

    @Query("""
        select j from ProcessingJob j, NarrationVersion v
        where v.id = j.targetVersionId
          and (:status is null or j.status = :status)
          and (:type is null or j.jobType = :type)
          and (:poiId is null or v.poiId = :poiId)
        order by j.id desc""")
    List<ProcessingJob> search(String status, String type, Long poiId);

    boolean existsByTargetVersionIdAndJobTypeAndTargetLanguageAndStatusIn(
            Long targetVersionId, String jobType, String targetLanguage, List<String> statuses);
}
