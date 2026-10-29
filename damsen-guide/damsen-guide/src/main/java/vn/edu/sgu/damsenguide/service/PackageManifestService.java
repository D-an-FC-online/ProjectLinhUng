package vn.edu.sgu.damsenguide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.sgu.damsenguide.dto.ManifestResponse;
import vn.edu.sgu.damsenguide.dto.ManifestResponse.*;
import vn.edu.sgu.damsenguide.entity.AudioAsset;
import vn.edu.sgu.damsenguide.entity.Poi;
import vn.edu.sgu.damsenguide.entity.PoiContent;
import vn.edu.sgu.damsenguide.repository.PoiRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PackageManifestService {

    private final PoiRepository poiRepository;

    @Value("${app.package.fixed-version:v1.0.0}")
    private String fixedPackageVersion;

    @Transactional(readOnly = true)
    public ManifestResponse getFixedManifest() {
        List<Poi> activePois = poiRepository.findAllActiveWithContents();
        List<PoiManifestItem> poiItems = new ArrayList<>();
        Map<Long, AudioManifestItem> uniqueAudios = new LinkedHashMap<>();

        for (Poi poi : activePois) {
            Map<String, LocalizedContentDto> contentMap = new HashMap<>();
            for (PoiContent c : poi.getContents()) {
                AudioAsset audio = c.getAudioAsset();
                Long audioId = (audio != null && "READY".equals(audio.getStatus())) ? audio.getId() : null;

                contentMap.put(c.getLanguageCode(), new LocalizedContentDto(
                    c.getTitle(),
                    c.getBody(),
                    audioId
                ));

                if (audioId != null && !uniqueAudios.containsKey(audioId)) {
                    uniqueAudios.put(audioId, new AudioManifestItem(
                        audio.getId(),
                        "/api/v1/public/media/audio/" + audio.getId(),
                        audio.getMimeType(),
                        audio.getDurationMs(),
                        audio.getFileSizeBytes(),
                        audio.getChecksumSha256()
                    ));
                }
            }

            poiItems.add(new PoiManifestItem(
                poi.getId(),
                poi.getCode(),
                poi.getLatitude(),
                poi.getLongitude(),
                poi.getNearRadiusM(),
                poi.getSortOrder(),
                contentMap
            ));
        }

        return new ManifestResponse(
            fixedPackageVersion,
            List.of("vi", "en", "ko"),
            poiItems,
            new ArrayList<>(uniqueAudios.values())
        );
    }
}
