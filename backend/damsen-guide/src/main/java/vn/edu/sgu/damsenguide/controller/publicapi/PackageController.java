package vn.edu.sgu.damsenguide.controller.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.damsenguide.dto.ManifestResponse;
import vn.edu.sgu.damsenguide.service.PackageManifestService;

@RestController
@RequestMapping("/api/v1/public/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageManifestService manifestService;

    @GetMapping("/current/manifest")
    public ResponseEntity<ManifestResponse> getCurrentManifest() {
        return ResponseEntity.ok(manifestService.getFixedManifest());
    }
}
