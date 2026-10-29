package vn.edu.sgu.damsenguide.controller.publicapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.sgu.damsenguide.dto.LocationCheckDto.*;
import vn.edu.sgu.damsenguide.service.NearbySuggestionService;

@RestController
@RequestMapping("/api/v1/public/pois")
@RequiredArgsConstructor
public class NearbyController {

    private final NearbySuggestionService suggestionService;

    // Dùng POST để tọa độ nằm trong body, không lộ trên URL log (BR05)
    @PostMapping("/nearby")
    public ResponseEntity<NearbySuggestionResponse> checkNearbyAndSuggest(
            @Valid @RequestBody NearbySuggestionRequest request) {
        return ResponseEntity.ok(suggestionService.evaluatePosition(request));
    }
}
