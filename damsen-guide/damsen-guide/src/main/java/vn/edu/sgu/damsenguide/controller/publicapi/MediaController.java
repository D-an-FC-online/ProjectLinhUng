package vn.edu.sgu.damsenguide.controller.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.damsenguide.service.AudioMediaService;
import vn.edu.sgu.damsenguide.service.AudioMediaService.AudioResourcePayload;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/public/media")
@RequiredArgsConstructor
public class MediaController {

    private final AudioMediaService audioMediaService;

    @GetMapping("/audio/{assetId}")
    public ResponseEntity<Resource> streamAudio(@PathVariable Long assetId) {
        AudioResourcePayload payload = audioMediaService.loadPublishedAudio(assetId);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(payload.mimeType()))
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .eTag(payload.checksum())
            .body(payload.resource()); // Spring MVC tự động xử lý HTTP Range (206 Partial Content)
    }
}
