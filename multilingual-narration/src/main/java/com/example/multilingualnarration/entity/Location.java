package com.example.multilingualnarration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Entity ánh xạ bảng {@code locations} — địa điểm tham quan.
 *
 * <p>Thiết kế có chủ đích: Location KHÔNG khai báo {@code @OneToMany List<Narration>}.
 * Quan hệ chỉ được khai báo một chiều ở phía {@link Narration} ({@code @ManyToOne}),
 * vì vậy lấy danh sách địa điểm không bao giờ kéo theo hàng loạt bản thuyết minh (tránh N+1
 * và tránh vòng lặp vô hạn khi chuyển sang JSON).
 *
 * <p>Entity chỉ dùng trong tầng Service/Repository, KHÔNG trả trực tiếp ra API.
 */
@Entity
@Table(name = "locations")
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", length = 500)
    private String address;

    /** Vĩ độ: DECIMAL(9,6) — độ chính xác ~0.1 m, đủ cho định vị địa điểm. */
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    /** Kinh độ: DECIMAL(9,6). */
    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /** JPA bắt buộc có constructor không tham số. */
    public Location() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
