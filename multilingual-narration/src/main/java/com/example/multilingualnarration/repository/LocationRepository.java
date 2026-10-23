package com.example.multilingualnarration.repository;

import com.example.multilingualnarration.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data Access Layer cho bảng {@code locations}.
 *
 * <p>Chỉ cần khai báo interface: khi khởi động, Spring Data JPA tự tạo class cài đặt (proxy)
 * và đăng ký thành Bean. Kế thừa {@link JpaRepository} có sẵn save, findById, findAll(Pageable),
 * deleteById, existsById...
 *
 * <p>Không chứa nghiệp vụ — chỉ truy vấn dữ liệu.
 */
public interface LocationRepository extends JpaRepository<Location, Long> {

    /**
     * Tìm kiếm theo từ khóa trong tên HOẶC địa chỉ, không phân biệt hoa thường, có phân trang.
     *
     * <p>Spring Data JPA đọc TÊN method để sinh câu truy vấn, tương đương:
     * <pre>
     * SELECT * FROM locations
     * WHERE UPPER(name) LIKE UPPER('%kw%') OR UPPER(address) LIKE UPPER('%kw%')
     * LIMIT ? OFFSET ?
     * </pre>
     * "Containing" tự thêm ký tự % và tự escape các ký tự đặc biệt (%, _) trong từ khóa.
     */
    Page<Location> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String nameKeyword, String addressKeyword, Pageable pageable);
}
