package vn.edu.sgu.damsenguide.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audio_assets")
@Getter @Setter
public class AudioAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_key", nullable = false, length = 300)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 50)
    private String mimeType = "audio/mpeg";

    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(nullable = false, length = 20)
    private String status = "READY";
}
