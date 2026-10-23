package com.example.multilingualnarration.controller;

import com.example.multilingualnarration.dto.request.CreateLanguageRequest;
import com.example.multilingualnarration.dto.request.UpdateLanguageRequest;
import com.example.multilingualnarration.dto.response.LanguageResponse;
import com.example.multilingualnarration.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
 * Presentation Layer — API ngôn ngữ hỗ trợ.
 *
 * <p>GET dùng cho mọi client (màn hình chọn ngôn ngữ).
 * POST/PUT là API quản trị (bổ sung so với danh sách ban đầu) để thêm ngôn ngữ mới hoặc
 * tạm tắt một ngôn ngữ mà không phải sửa code; đồng thời là nơi áp dụng quy tắc chống trùng mã.
 */
@RestController
@RequestMapping("/api/v1/languages")
@Tag(name = "Languages", description = "Ngôn ngữ được hệ thống hỗ trợ")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách ngôn ngữ được hỗ trợ")
    public List<LanguageResponse> getAll(
            @Parameter(description = "true = lấy cả ngôn ngữ đã tắt (dùng cho quản trị)")
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return languageService.getAll(includeInactive);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Lấy chi tiết ngôn ngữ theo mã (vi, en, ja, ko, zh...)",
            description = "Trường active cho biết ngôn ngữ có đang được kích hoạt hay không.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "LANGUAGE_NOT_FOUND")
    })
    public LanguageResponse getByCode(@PathVariable String code) {
        return languageService.getByCode(code);
    }

    @PostMapping
    @Operation(summary = "[Quản trị] Thêm ngôn ngữ mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
            @ApiResponse(responseCode = "409", description = "LANGUAGE_ALREADY_EXISTS")
    })
    public ResponseEntity<LanguageResponse> create(@Valid @RequestBody CreateLanguageRequest request) {
        LanguageResponse created = languageService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/languages/" + created.code())).body(created);
    }

    @PutMapping("/{code}")
    @Operation(summary = "[Quản trị] Cập nhật tên / bật-tắt ngôn ngữ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR"),
            @ApiResponse(responseCode = "404", description = "LANGUAGE_NOT_FOUND")
    })
    public LanguageResponse update(@PathVariable String code, @Valid @RequestBody UpdateLanguageRequest request) {
        return languageService.update(code, request);
    }
}
