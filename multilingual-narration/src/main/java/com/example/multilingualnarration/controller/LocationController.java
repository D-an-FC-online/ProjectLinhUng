package com.example.multilingualnarration.controller;

import com.example.multilingualnarration.dto.request.CreateLocationRequest;
import com.example.multilingualnarration.dto.request.UpdateLocationRequest;
import com.example.multilingualnarration.dto.response.LocationResponse;
import com.example.multilingualnarration.dto.response.PageResponse;
import com.example.multilingualnarration.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

/**
 * Presentation Layer — API quản lý địa điểm tham quan.
 *
 * <p>Nhiệm vụ của Controller CHỈ gồm: nhận HTTP request, kích hoạt validation ({@code @Valid}),
 * gọi Service và trả HTTP response với status code phù hợp.
 * Không chứa nghiệp vụ, không gọi Repository, không biết đến Entity.
 */
@RestController
@RequestMapping("/api/v1/locations")
@Tag(name = "Locations", description = "Quản lý địa điểm tham quan")
public class LocationController {

    private final LocationService locationService;

    /** Constructor injection: Spring tự truyền Bean LocationServiceImpl vào (không dùng "new"). */
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @Operation(summary = "Tạo địa điểm mới")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo thành công, header Location chứa URL của địa điểm"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / INVALID_COORDINATES")
    })
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest request) {
        LocationResponse created = locationService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/locations/" + created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách địa điểm (phân trang)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / INVALID_SORT")
    })
    public PageResponse<LocationResponse> getAll(
            @Parameter(description = "Số trang, bắt đầu từ 0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @Parameter(description = "Số phần tử mỗi trang (1-100)")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size phải >= 1")
            @Max(value = 100, message = "size tối đa 100") int size,
            @Parameter(description = "Sắp xếp theo: id, name, createdAt, updatedAt")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "asc hoặc desc")
            @RequestParam(defaultValue = "asc") String direction) {
        return locationService.getAll(page, size, sortBy, direction);
    }

    /** Đặt trước "/{id}" cho dễ đọc; Spring luôn ưu tiên đường dẫn cố định "/search" hơn biến {id}. */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm địa điểm theo từ khóa (tên hoặc địa chỉ)",
            description = "Không phân biệt hoa thường. Với collation utf8mb4_unicode_ci của MySQL, "
                    + "tìm 'van mieu' vẫn ra 'Văn Miếu'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công (có thể là danh sách rỗng)"),
            @ApiResponse(responseCode = "400", description = "Thiếu hoặc rỗng keyword, tham số phân trang sai")
    })
    public PageResponse<LocationResponse> search(
            @RequestParam @NotBlank(message = "keyword không được để trống")
            @Size(max = 100, message = "keyword tối đa 100 ký tự") String keyword,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size phải >= 1")
            @Max(value = 100, message = "size tối đa 100") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return locationService.search(keyword, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết địa điểm")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND")
    })
    public LocationResponse getById(@PathVariable Long id) {
        return locationService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật địa điểm (thay thế toàn bộ thông tin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR / INVALID_COORDINATES"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND")
    })
    public LocationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateLocationRequest request) {
        return locationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa điểm",
            description = "Chỉ xóa được khi địa điểm KHÔNG còn bản thuyết minh nào.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "LOCATION_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "LOCATION_HAS_NARRATIONS")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
