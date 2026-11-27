package vn.edu.sgu.damsen.web;

import java.time.Duration;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.damsen.domain.AudioAsset;
import vn.edu.sgu.damsen.service.AudioStorage;
import vn.edu.sgu.damsen.service.PublicContentService;
import vn.edu.sgu.damsen.web.dto.Dtos.*;

/** API công khai cho khách tham quan. Tham số lang = vi | en | ko (thiếu -> vi). */
@RestController
@RequestMapping("/api/v1/public")
public class PublicApiController {
    private final PublicContentService content;
    private final AudioStorage storage;

    public PublicApiController(PublicContentService content, AudioStorage storage) {
        this.content = content;
        this.storage = storage;
    }

    @GetMapping("/languages")
    public List<LanguageDto> languages() {
        return content.languages();
    }

    /** Danh sách khu vực đang bật (UC03). */
    @GetMapping("/zones")
    public List<ZoneDto> zones(@RequestParam(defaultValue = "vi") String lang) {
        return content.zones(lang);
    }

    /** Duyệt thủ công: lọc theo khu vực, tìm từ khóa, phân trang (UC03). */
    @GetMapping("/pois")
    public PageDto<PoiSummary> pois(@RequestParam(required = false) Long zoneId,
                                    @RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "vi") String lang,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return content.list(zoneId, q, lang, page, size);
    }

    /** Tìm điểm gần vị trí (UC02). radius mặc định 300m. */
    @GetMapping("/pois/nearby")
    public List<PoiSummary> nearby(@RequestParam double lat, @RequestParam double lng,
                                   @RequestParam(required = false) Integer radius,
                                   @RequestParam(defaultValue = "vi") String lang) {
        return content.nearby(lat, lng, radius, lang);
    }

    /** Xem nội dung thuyết minh của một điểm (UC04). */
    @GetMapping("/pois/{id}")
    public PoiDetail poi(@PathVariable Long id, @RequestParam(defaultValue = "vi") String lang) {
        return content.detail(id, lang);
    }

    /** Phát âm thanh (UC05). Trả về Resource nên Spring tự hỗ trợ HTTP Range (tua được). */
    @GetMapping("/audio/{id}")
    public ResponseEntity<Resource> audio(@PathVariable Long id) {
        AudioAsset a = content.publishedAudio(id);
        return ResponseEntity.ok()
                .contentType(storage.mediaType(a.getStorageKey()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .eTag(a.getId() + "-" + (a.getContentHash() == null ? a.getStorageKey() : a.getContentHash()))
                .body(storage.read(a.getStorageKey()));
    }
}
