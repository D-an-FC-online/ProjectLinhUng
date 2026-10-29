package vn.edu.sgu.damsenguide.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.sgu.damsenguide.dto.LocationCheckDto.*;
import vn.edu.sgu.damsenguide.entity.Poi;
import vn.edu.sgu.damsenguide.entity.PoiContent;
import vn.edu.sgu.damsenguide.repository.PoiRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NearbySuggestionService {

    private final PoiRepository poiRepository;
    private final GeoDistanceCalculator geoCalculator;

    @Value("${app.package.fixed-version:v1.0.0}")
    private String fixedPackageVersion;

    @Value("${app.geo.park-center-lat:10.767600}")
    private double parkCenterLat;

    @Value("${app.geo.park-center-lng:106.640600}")
    private double parkCenterLng;

    @Value("${app.geo.park-radius-m:1500}")
    private double parkRadiusM;

    @Value("${app.geo.accuracy-good-m:30}")
    private double accuracyGoodM;

    @Value("${app.geo.accuracy-fair-m:100}")
    private double accuracyFairM;

    @Value("${app.geo.max-location-age-ms:60000}")
    private long maxLocationAgeMs;

    @Transactional(readOnly = true)
    public NearbySuggestionResponse evaluatePosition(NearbySuggestionRequest req) {
        // 1. Kiểm tra phiên bản dữ liệu cố định
        if (!fixedPackageVersion.equals(req.packageVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PACKAGE_VERSION_MISMATCH");
        }

        // 2. Kiểm tra số hữu hạn (chống NaN / Infinity)
        if (!Double.isFinite(req.latitude()) || !Double.isFinite(req.longitude())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_LOCATION");
        }

        // 3. Phân loại chất lượng vị trí theo tuổi vị trí và sai số (BR10)
        String quality = classifyQuality(req.accuracyM(), req.locationAgeMs());

        // 4. Kiểm tra vị trí có nằm trong bán kính khu vực công viên không (BR11)
        double distanceToParkCenter = geoCalculator.calculateHaversineMeters(
            req.latitude(), req.longitude(), parkCenterLat, parkCenterLng
        );
        if (distanceToParkCenter > parkRadiusM) {
            return new NearbySuggestionResponse(fixedPackageVersion, quality, true, null, List.of());
        }

        // 5. Nếu tín hiệu POOR hoặc STALE -> Không đủ tin cậy để nhận diện điểm gần (BR15), trả kết quả rỗng
        if ("POOR".equals(quality) || "STALE".equals(quality)) {
            return new NearbySuggestionResponse(fixedPackageVersion, quality, false, null, List.of());
        }

        // 6. Lọc các điểm ACTIVE thỏa mãn bán kính nhận diện riêng (distance <= poi.nearRadiusM)
        List<Poi> activePois = poiRepository.findAllActiveWithContents();
        record Candidate(Poi poi, double exactDistanceM, PoiContent content, boolean fallback) {}
        List<Candidate> matchedCandidates = new ArrayList<>();

        for (Poi poi : activePois) {
            double dist = geoCalculator.calculateHaversineMeters(
                req.latitude(), req.longitude(),
                poi.getLatitude().doubleValue(), poi.getLongitude().doubleValue()
            );

            // Chỉ lấy điểm nằm trong bán kính nhận diện nearRadiusM của chính điểm đó
            if (dist <= poi.getNearRadiusM()) {
                PoiContent targetContent = findContentByLang(poi.getContents(), req.language());
                boolean isFallback = false;

                if (targetContent == null) {
                    targetContent = findContentByLang(poi.getContents(), "vi");
                    isFallback = true;
                }

                if (targetContent != null) {
                    matchedCandidates.add(new Candidate(poi, dist, targetContent, isFallback));
                }
            }
        }

        // 7. Nếu không có điểm nào trong bán kính nhận diện -> Trả rỗng, KHÔNG chọn đại điểm gần nhất
        if (matchedCandidates.isEmpty()) {
            return new NearbySuggestionResponse(fixedPackageVersion, quality, false, null, List.of());
        }

        // 8. Sắp xếp theo khoảng cách tăng dần; nếu chênh lệch < 1m thì xếp theo sortOrder rồi theo mã (BR09)
        matchedCandidates.sort((a, b) -> {
            if (Math.abs(a.exactDistanceM() - b.exactDistanceM()) >= 1.0) {
                return Double.compare(a.exactDistanceM(), b.exactDistanceM());
            }
            int sortCmp = Integer.compare(a.poi().getSortOrder(), b.poi().getSortOrder());
            return (sortCmp != 0) ? sortCmp : a.poi().getCode().compareTo(b.poi().getCode());
        });

        List<EligiblePoiItem> eligibleItems = matchedCandidates.stream()
            .map(c -> new EligiblePoiItem(
                c.poi().getId(),
                c.poi().getCode(),
                c.content().getTitle(),
                c.content().getLanguageCode(),
                c.fallback(),
                Math.round(c.exactDistanceM()),
                c.poi().getNearRadiusM(),
                (c.content().getAudioAsset() != null && "READY".equals(c.content().getAudioAsset().getStatus()))
                    ? c.content().getAudioAsset().getId() : null
            ))
            .toList();

        return new NearbySuggestionResponse(
            fixedPackageVersion,
            quality,
            false,
            eligibleItems.get(0), // Điểm đủ điều kiện gần nhất
            eligibleItems
        );
    }

    private String classifyQuality(Double accuracyM, Long locationAgeMs) {
        if (locationAgeMs != null && locationAgeMs > maxLocationAgeMs) {
            return "STALE";
        }
        if (accuracyM == null || accuracyM > accuracyFairM) {
            return "POOR";
        }
        if (accuracyM <= accuracyGoodM) {
            return "GOOD";
        }
        return "FAIR";
    }

    private PoiContent findContentByLang(List<PoiContent> contents, String lang) {
        return contents.stream()
            .filter(c -> c.getLanguageCode().equalsIgnoreCase(lang))
            .findFirst()
            .orElse(null);
    }
}
