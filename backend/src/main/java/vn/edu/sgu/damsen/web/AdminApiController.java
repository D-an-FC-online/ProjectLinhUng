package vn.edu.sgu.damsen.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.sgu.damsen.domain.AudioAsset;
import vn.edu.sgu.damsen.service.*;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/** API quản trị (yêu cầu vai trò ADMIN). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {
    private final CatalogAdminService catalog;
    private final NarrationService narration;
    private final PublicationService publication;
    private final JobService jobs;
    private final TranslationService translation;
    private final AudioStorage storage;

    public AdminApiController(CatalogAdminService catalog, NarrationService narration, PublicationService publication,
                              JobService jobs, TranslationService translation, AudioStorage storage) {
        this.catalog = catalog;
        this.narration = narration;
        this.publication = publication;
        this.jobs = jobs;
        this.translation = translation;
        this.storage = storage;
    }

    @GetMapping("/overview")
    public OverviewDto overview() {
        return publication.overview(translation.isEnabled());
    }

    /* ----------------------------- Zones ----------------------------- */

    @GetMapping("/zones")
    public List<ZoneAdminDto> zones() { return catalog.listZones(); }

    @PostMapping("/zones")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createZone(@Valid @RequestBody ZoneRequest r) {
        return Map.of("id", catalog.saveZone(null, r));
    }

    @PutMapping("/zones/{id}")
    public Map<String, Long> updateZone(@PathVariable Long id, @Valid @RequestBody ZoneRequest r) {
        return Map.of("id", catalog.saveZone(id, r));
    }

    /* ------------------------------ POIs ------------------------------ */

    @GetMapping("/pois")
    public List<PoiAdminDto> pois() { return catalog.listPois(); }

    @PostMapping("/pois")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createPoi(@Valid @RequestBody PoiRequest r) {
        return Map.of("id", catalog.savePoi(null, r));
    }

    @PutMapping("/pois/{id}")
    public Map<String, Long> updatePoi(@PathVariable Long id, @Valid @RequestBody PoiRequest r) {
        return Map.of("id", catalog.savePoi(id, r));
    }

    /** Toàn bộ nội dung của một điểm: các ngôn ngữ, phiên bản, audio, trạng thái xuất bản. */
    @GetMapping("/pois/{id}/content")
    public PoiContentDto content(@PathVariable Long id) {
        return new PoiContentDto(catalog.getPoi(id), narration.content(id));
    }

    @PostMapping("/pois/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createDraft(@PathVariable Long id, @Valid @RequestBody DraftRequest r) {
        return Map.of("id", narration.createDraft(id, r));
    }

    @PostMapping("/pois/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest r) {
        publication.publish(id, r);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/pois/{id}/publications/{lang}")
    public ResponseEntity<Void> unpublish(@PathVariable Long id, @PathVariable String lang) {
        publication.unpublish(id, lang);
        return ResponseEntity.noContent().build();
    }

    /* --------------------------- Versions --------------------------- */

    @GetMapping("/versions/{id}")
    public VersionDto version(@PathVariable Long id) { return narration.version(id); }

    @PutMapping("/versions/{id}")
    public ResponseEntity<Void> updateDraft(@PathVariable Long id, @Valid @RequestBody DraftUpdateRequest r) {
        narration.updateDraft(id, r);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/versions/{id}/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> copy(@PathVariable Long id) {
        return Map.of("id", narration.copyToDraft(id));
    }

    @PostMapping("/versions/{id}/discard")
    public ResponseEntity<Void> discard(@PathVariable Long id) {
        narration.discardDraft(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/versions/{id}/qa")
    public QaResult qa(@PathVariable Long id) { return narration.qa(id); }

    @PostMapping("/versions/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        narration.approve(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/versions/{id}/translate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public JobDto translate(@PathVariable Long id, @Valid @RequestBody TranslateRequest r) {
        return jobs.requestTranslate(id, r.targetLanguage());
    }

    @PostMapping("/versions/{id}/tts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public JobDto tts(@PathVariable Long id) { return jobs.requestTts(id); }

    @PostMapping(value = "/versions/{id}/audio", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return Map.of("id", narration.uploadAudio(id, file));
    }

    /** Nghe thử bất kỳ audio nào (kể cả chưa xuất bản). */
    @GetMapping("/audio/{id}")
    public ResponseEntity<Resource> audio(@PathVariable Long id) {
        AudioAsset a = narration.audio(id);
        return ResponseEntity.ok().contentType(storage.mediaType(a.getStorageKey())).body(storage.read(a.getStorageKey()));
    }

    /* ----------------------------- Jobs ----------------------------- */

    @GetMapping("/jobs")
    public List<JobDto> jobs(@RequestParam(required = false) String status, @RequestParam(required = false) String type,
                             @RequestParam(required = false) Long poiId) {
        return jobs.search(status, type, poiId);
    }

    @GetMapping("/jobs/{id}")
    public JobDto job(@PathVariable Long id) { return jobs.get(id); }

    @PostMapping("/jobs/{id}/retry")
    public JobDto retry(@PathVariable Long id) { return jobs.retry(id); }

    @PostMapping("/jobs/{id}/cancel")
    public JobDto cancel(@PathVariable Long id) { return jobs.cancel(id); }

    /* --------------------------- Glossary --------------------------- */

    @GetMapping("/glossary")
    public List<GlossaryDto> glossary() { return catalog.listGlossary(); }

    @PostMapping("/glossary")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> createTerm(@Valid @RequestBody GlossaryDto r) {
        return Map.of("id", catalog.saveGlossary(null, r));
    }

    @PutMapping("/glossary/{id}")
    public Map<String, Long> updateTerm(@PathVariable Long id, @Valid @RequestBody GlossaryDto r) {
        return Map.of("id", catalog.saveGlossary(id, r));
    }

    @DeleteMapping("/glossary/{id}")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        catalog.deleteGlossary(id);
        return ResponseEntity.noContent().build();
    }
}
