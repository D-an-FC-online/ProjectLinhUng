package com.example.multilingualnarration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * Lớp cha chứa 2 cột thời gian dùng chung {@code created_at}, {@code updated_at}.
 *
 * <p>{@code @MappedSuperclass}: KHÔNG phải một bảng riêng; các cột được "kế thừa" vào bảng của
 * Entity con (locations, narrations).
 *
 * <p>{@code @PrePersist} / {@code @PreUpdate} là JPA lifecycle callback: Hibernate tự gọi ngay
 * trước khi INSERT / UPDATE, nên tầng Service không phải nhớ set thời gian.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
