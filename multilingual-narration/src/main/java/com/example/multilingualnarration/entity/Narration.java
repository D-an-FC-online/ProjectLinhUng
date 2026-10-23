package com.example.multilingualnarration.entity;

import com.example.multilingualnarration.enums.AudioStatus;
import com.example.multilingualnarration.enums.NarrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entity ánh xạ bảng {@code narrations} — nội dung thuyết minh của MỘT địa điểm bằng MỘT ngôn ngữ.
 *
 * <p>Quy tắc nghiệp vụ được bảo vệ ở cả 2 tầng:
 * <ul>
 *   <li>Tầng Service kiểm tra trước và trả lỗi 409 dễ hiểu.</li>
 *   <li>Database có unique constraint {@code uk_narrations_location_language} để chặn trường hợp
 *       2 request đồng thời cùng vượt qua bước kiểm tra (race condition).</li>
 * </ul>
 *
 * <p>Cả hai quan hệ dùng {@code FetchType.LAZY}: chỉ nạp Location/Language khi thật sự cần.
 * Ở những truy vấn cần dữ liệu liên quan, Repository dùng {@code @EntityGraph} / {@code join fetch}
 * để lấy tất cả trong MỘT câu SQL (tránh N+1).
 */
@Entity
@Table(name = "narrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_narrations_location_language",
                columnNames = {"location_id", "language_id"}))
public class Narration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Nội dung dài => MEDIUMTEXT (tối đa ~16 MB), đủ cho mọi ngôn ngữ kể cả chữ CJK 3-4 byte. */
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    /** URL file âm thanh do module Text-to-Speech tạo ra; null nếu chưa có. */
    @Column(name = "audio_url", length = 1000)
    private String audioUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NarrationStatus status = NarrationStatus.DRAFT;

    public Narration() {
    }

    /** Trạng thái âm thanh được suy ra từ audioUrl, không lưu trùng trong database. */
    public AudioStatus getAudioStatus() {
        return (audioUrl == null || audioUrl.isBlank()) ? AudioStatus.NOT_AVAILABLE : AudioStatus.AVAILABLE;
    }

    public boolean isPublished() {
        return status == NarrationStatus.PUBLISHED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public NarrationStatus getStatus() {
        return status;
    }

    public void setStatus(NarrationStatus status) {
        this.status = status;
    }
}
