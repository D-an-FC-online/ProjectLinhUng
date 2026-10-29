package vn.edu.sgu.damsenguide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.sgu.damsenguide.entity.AudioAsset;
import vn.edu.sgu.damsenguide.repository.AudioAssetRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class AudioMediaService {

    private final AudioAssetRepository audioAssetRepository;

    @Value("${app.media.root:/data/media}")
    private String mediaRoot;

    public record AudioResourcePayload(Resource resource, String mimeType, String checksum) {}

    @Transactional(readOnly = true)
    public AudioResourcePayload loadPublishedAudio(Long assetId) {
        AudioAsset asset = audioAssetRepository.findActivePublishedAsset(assetId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AUDIO_NOT_AVAILABLE"));

        Path filePath = Paths.get(mediaRoot).resolve(asset.getStorageKey()).normalize();
        if (!filePath.startsWith(Paths.get(mediaRoot).normalize()) || !Files.exists(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AUDIO_FILE_MISSING");
        }

        return new AudioResourcePayload(
            new FileSystemResource(filePath),
            asset.getMimeType(),
            asset.getChecksumSha256()
        );
    }
}
