package com.example.multilingualnarration.controller;

import com.example.multilingualnarration.dto.request.AttachAudioRequest;
import com.example.multilingualnarration.dto.request.CreateNarrationRequest;
import com.example.multilingualnarration.dto.request.TranslateNarrationRequest;
import com.example.multilingualnarration.dto.request.UpdateNarrationRequest;
import com.example.multilingualnarration.dto.response.AvailableLanguageResponse;
import com.example.multilingualnarration.dto.response.NarrationLookupResponse;
import com.example.multilingualnarration.dto.response.NarrationResponse;
import com.example.multilingualnarration.enums.NarrationStatus;
import com.example.multilingualnarration.service.NarrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Presentation Layer — API nội dung thuyết minh.
 *
 * <p>Đường dẫn chia 2 dạng theo quan hệ cha-con:
 * <ul>
 *   <li>{@code /api/v1/locations/{locationId}/narrations...}: thao tác "trong phạm vi một địa điểm"</li>
 *   <li>{@code /api/v1/narrations/{id}...}: thao tác trên một bản thuyết minh cụ thể</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Narrations", description = "Nội dung thuyết minh đa ngôn ngữ và điểm tích hợp dịch thuật / TTS")
public class NarrationController {

    private final NarrationService narrationService;

    public NarrationController(NarrationService narrationService) {
        this.narrationService = narrationService;
    }

    // ------------------------------------------------------------------
    // Trong phạm vi một địa điểm
    // ------------------------------------------------------------------

    @PostMapping("/locations/{locationId}/narrations")
    @Operation(summary = "Tạo bản thuyết minh cho địa điểm",
            description = "Mỗi địa điểm chỉ có 1 bản cho mỗi ngôn ngữ. status mặc định là DRAFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / LANGUAGE_INACTIVE / INVALID_REQUEST"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND / LANGUAGE_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "NARRATION_ALREADY_EXISTS")
    })
    public ResponseEntity<NarrationResponse> create(@PathVariable Long locationId,
                                                    @Valid @RequestBody CreateNarrationRequest request) {
        NarrationResponse created = narrationService.create(locationId, request);
        return ResponseEntity.created(URI.create("/api/v1/narrations/" + created.id())).body(created);
    }

    @GetMapping("/locations/{locationId}/narrations")
    @Operation(summary = "Tra cứu thuyết minh của địa điểm theo ngôn ngữ (dành cho khách tham quan)",
            description = "Luôn trả 200 kèm availability: AVAILABLE (có nội dung), PENDING (đang chờ duyệt), "
                    + "NOT_AVAILABLE (chưa có). Chỉ trả nội dung khi AVAILABLE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công, đọc trường availability"),
            @ApiResponse(responseCode = "400", description = "Thiếu tham số language / LANGUAGE_INACTIVE"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND / LANGUAGE_NOT_FOUND")
    })
    public NarrationLookupResponse lookup(
            @PathVariable Long locationId,
            @Parameter(description = "Mã ngôn ngữ, ví dụ: vi, en, ja, ko, zh", example = "vi")
            @RequestParam @NotBlank(message = "language không được để trống")
            @Size(max = 10, message = "language tối đa 10 ký tự") String language) {
        return narrationService.lookup(locationId, language);
    }

    @GetMapping("/locations/{locationId}/narrations/available-languages")
    @Operation(summary = "Liệt kê các ngôn ngữ đã có bản thuyết minh cho địa điểm",
            description = "Truyền status=PUBLISHED để chỉ lấy ngôn ngữ khách xem được.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công (có thể rỗng)"),
            @ApiResponse(responseCode = "400", description = "status không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND")
    })
    public List<AvailableLanguageResponse> getAvailableLanguages(
            @PathVariable Long locationId,
            @Parameter(description = "Lọc theo trạng thái: DRAFT hoặc PUBLISHED. Bỏ trống = tất cả")
            @RequestParam(required = false) NarrationStatus status) {
        return narrationService.getAvailableLanguages(locationId, status);
    }

    // ------------------------------------------------------------------
    // Trên một bản thuyết minh cụ thể
    // ------------------------------------------------------------------

    @GetMapping("/narrations/{id}")
    @Operation(summary = "Lấy bản thuyết minh theo ID (mọi trạng thái, dùng cho quản trị)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND")
    })
    public NarrationResponse getById(@PathVariable Long id) {
        return narrationService.getById(id);
    }

    @PutMapping("/narrations/{id}")
    @Operation(summary = "Cập nhật tiêu đề, nội dung, trạng thái",
            description = "Nếu nội dung thay đổi, audioUrl cũ tự động bị xóa (audioStatus = NOT_AVAILABLE).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / INVALID_REQUEST"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND")
    })
    public NarrationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateNarrationRequest request) {
        return narrationService.update(id, request);
    }

    @DeleteMapping("/narrations/{id}")
    @Operation(summary = "Xóa bản thuyết minh", description = "Chỉ xóa được bản ở trạng thái DRAFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "NARRATION_PUBLISHED")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        narrationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Điểm tích hợp với module Dịch thuật AI và Text-to-Speech
    // ------------------------------------------------------------------

    @PutMapping("/narrations/{id}/audio")
    @Operation(summary = "[Tích hợp TTS] Gắn URL âm thanh đã tạo",
            description = "Dành cho module TTS chạy như dịch vụ riêng: tạo file xong gọi API này để lưu URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã gắn âm thanh"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND")
    })
    public NarrationResponse attachAudio(@PathVariable Long id, @Valid @RequestBody AttachAudioRequest request) {
        return narrationService.attachAudio(id, request);
    }

    @PostMapping("/narrations/{id}/translations")
    @Operation(summary = "[Tích hợp AI] Dịch bản thuyết minh sang ngôn ngữ khác",
            description = "Gọi TranslationProvider và tạo bản mới ở trạng thái DRAFT (chờ người duyệt). "
                    + "Trả 503 nếu chưa cấu hình dịch vụ dịch thuật.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đã tạo bản dịch (DRAFT)"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / SAME_LANGUAGE / LANGUAGE_INACTIVE"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND / LANGUAGE_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "NARRATION_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "502", description = "TRANSLATION_FAILED"),
            @ApiResponse(responseCode = "503", description = "TRANSLATION_PROVIDER_UNAVAILABLE")
    })
    public ResponseEntity<NarrationResponse> translate(@PathVariable Long id,
                                                       @Valid @RequestBody TranslateNarrationRequest request) {
        NarrationResponse created = narrationService.translate(id, request);
        return ResponseEntity.created(URI.create("/api/v1/narrations/" + created.id())).body(created);
    }

    @PostMapping("/narrations/{id}/audio/generate")
    @Operation(summary = "[Tích hợp TTS] Tạo âm thanh bằng TextToSpeechProvider",
            description = "Trả 503 nếu chưa cấu hình dịch vụ Text-to-Speech.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã tạo âm thanh"),
            @ApiResponse(responseCode = "404", description = "NARRATION_NOT_FOUND"),
            @ApiResponse(responseCode = "502", description = "TTS_FAILED"),
            @ApiResponse(responseCode = "503", description = "TTS_PROVIDER_UNAVAILABLE")
    })
    public NarrationResponse generateAudio(@PathVariable Long id) {
        return narrationService.generateAudio(id);
    }
}
