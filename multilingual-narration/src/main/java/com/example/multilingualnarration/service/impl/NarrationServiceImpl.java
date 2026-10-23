package com.example.multilingualnarration.service.impl;

import com.example.multilingualnarration.dto.request.AttachAudioRequest;
import com.example.multilingualnarration.dto.request.CreateNarrationRequest;
import com.example.multilingualnarration.dto.request.TranslateNarrationRequest;
import com.example.multilingualnarration.dto.request.UpdateNarrationRequest;
import com.example.multilingualnarration.dto.response.AvailableLanguageResponse;
import com.example.multilingualnarration.dto.response.NarrationLookupResponse;
import com.example.multilingualnarration.dto.response.NarrationResponse;
import com.example.multilingualnarration.entity.Language;
import com.example.multilingualnarration.entity.Location;
import com.example.multilingualnarration.entity.Narration;
import com.example.multilingualnarration.enums.AudioStatus;
import com.example.multilingualnarration.enums.NarrationAvailability;
import com.example.multilingualnarration.enums.NarrationStatus;
import com.example.multilingualnarration.exception.BusinessException;
import com.example.multilingualnarration.exception.DuplicateResourceException;
import com.example.multilingualnarration.exception.ErrorCode;
import com.example.multilingualnarration.exception.IntegrationException;
import com.example.multilingualnarration.exception.ResourceNotFoundException;
import com.example.multilingualnarration.integration.TextToSpeechProvider;
import com.example.multilingualnarration.integration.TranslationProvider;
import com.example.multilingualnarration.mapper.NarrationMapper;
import com.example.multilingualnarration.repository.LanguageRepository;
import com.example.multilingualnarration.repository.LocationRepository;
import com.example.multilingualnarration.repository.NarrationRepository;
import com.example.multilingualnarration.service.NarrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Cài đặt nghiệp vụ nội dung thuyết minh.
 *
 * <p>Các quy tắc nghiệp vụ chính:
 * <ol>
 *   <li>Địa điểm và ngôn ngữ phải tồn tại; ngôn ngữ phải đang được kích hoạt.</li>
 *   <li>Mỗi (địa điểm, ngôn ngữ) chỉ có tối đa 1 bản thuyết minh.</li>
 *   <li>Khách chỉ xem được bản PUBLISHED; bản DRAFT được báo là PENDING.</li>
 *   <li>Sửa nội dung => audio cũ không còn khớp => tự xóa audioUrl để TTS tạo lại.</li>
 *   <li>Không xóa trực tiếp bản đang PUBLISHED (phải chuyển về DRAFT trước).</li>
 *   <li>Bản dịch tự động luôn được tạo ở trạng thái DRAFT để con người duyệt trước khi công bố.</li>
 * </ol>
 */
@Service
@Transactional(readOnly = true)
public class NarrationServiceImpl implements NarrationService {

    private static final Logger log = LoggerFactory.getLogger(NarrationServiceImpl.class);
    private static final int TITLE_MAX_LENGTH = 255;

    private final NarrationRepository narrationRepository;
    private final LocationRepository locationRepository;
    private final LanguageRepository languageRepository;
    private final NarrationMapper narrationMapper;

    /**
     * Optional: nếu CHƯA có Bean nào cài đặt TranslationProvider / TextToSpeechProvider,
     * Spring tiêm Optional.empty() thay vì báo lỗi khởi động. Nhờ vậy Core Backend chạy được
     * độc lập, và tự động dùng provider ngay khi thành viên khác bổ sung.
     */
    private final Optional<TranslationProvider> translationProvider;
    private final Optional<TextToSpeechProvider> textToSpeechProvider;

    public NarrationServiceImpl(NarrationRepository narrationRepository,
                                LocationRepository locationRepository,
                                LanguageRepository languageRepository,
                                NarrationMapper narrationMapper,
                                Optional<TranslationProvider> translationProvider,
                                Optional<TextToSpeechProvider> textToSpeechProvider) {
        this.narrationRepository = narrationRepository;
        this.locationRepository = locationRepository;
        this.languageRepository = languageRepository;
        this.narrationMapper = narrationMapper;
        this.translationProvider = translationProvider;
        this.textToSpeechProvider = textToSpeechProvider;
    }

    // =====================================================================
    // QUẢN LÝ NỘI DUNG
    // =====================================================================

    @Override
    @Transactional
    public NarrationResponse create(Long locationId, CreateNarrationRequest request) {
        Location location = findLocationOrThrow(locationId);
        Language language = findActiveLanguageOrThrow(request.languageCode());
        ensureNarrationNotExists(location, language);

        Narration narration = new Narration();
        narration.setLocation(location);
        narration.setLanguage(language);
        narration.setTitle(request.title().trim());
        narration.setContent(request.content().trim());
        narration.setAudioUrl(trimToNull(request.audioUrl()));
        narration.setStatus(request.status() != null ? request.status() : NarrationStatus.DRAFT);

        return narrationMapper.toResponse(narrationRepository.save(narration));
    }

    @Override
    public NarrationResponse getById(Long id) {
        return narrationMapper.toResponse(findNarrationWithDetailsOrThrow(id));
    }

    @Override
    @Transactional
    public NarrationResponse update(Long id, UpdateNarrationRequest request) {
        Narration narration = findNarrationWithDetailsOrThrow(id);

        String newContent = request.content().trim();
        boolean contentChanged = !newContent.equals(narration.getContent());

        narration.setTitle(request.title().trim());
        narration.setContent(newContent);
        narration.setStatus(request.status());

        if (contentChanged && narration.getAudioUrl() != null) {
            // Audio được đọc từ nội dung cũ => không còn đúng => xóa để module TTS tạo lại
            log.info("Nội dung narration id={} thay đổi, xóa audioUrl cũ để tạo lại âm thanh", id);
            narration.setAudioUrl(null);
        }
        return narrationMapper.toResponse(narrationRepository.saveAndFlush(narration));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Narration narration = narrationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.narration(id));
        if (narration.isPublished()) {
            throw new BusinessException(ErrorCode.NARRATION_PUBLISHED,
                    "Không thể xóa bản thuyết minh đang PUBLISHED. Hãy chuyển về DRAFT trước khi xóa");
        }
        narrationRepository.delete(narration);
    }

    @Override
    public NarrationLookupResponse lookup(Long locationId, String languageCode) {
        Location location = findLocationOrThrow(locationId);
        Language language = findActiveLanguageOrThrow(languageCode);

        // 1 truy vấn duy nhất (join fetch language) dùng cho cả 2 mục đích:
        // tìm bản đúng ngôn ngữ + liệt kê các ngôn ngữ đã PUBLISHED
        List<Narration> narrations = narrationRepository.findAllWithActiveLanguageByLocationId(locationId);
        List<String> publishedLanguages = narrations.stream()
                .filter(Narration::isPublished)
                .map(n -> n.getLanguage().getCode())
                .toList();
        Optional<Narration> found = narrations.stream()
                .filter(n -> n.getLanguage().getCode().equals(language.getCode()))
                .findFirst();

        if (found.isEmpty()) {
            return new NarrationLookupResponse(location.getId(), location.getName(), language.getCode(),
                    NarrationAvailability.NOT_AVAILABLE, AudioStatus.NOT_AVAILABLE,
                    "Chưa có nội dung thuyết minh " + language.getName() + " cho địa điểm này",
                    null, publishedLanguages);
        }

        Narration narration = found.get();
        if (!narration.isPublished()) {
            return new NarrationLookupResponse(location.getId(), location.getName(), language.getCode(),
                    NarrationAvailability.PENDING, AudioStatus.NOT_AVAILABLE,
                    "Nội dung thuyết minh " + language.getName() + " đang được chuẩn bị (chờ duyệt)",
                    null, publishedLanguages);
        }

        AudioStatus audioStatus = narration.getAudioStatus();
        String message = audioStatus == AudioStatus.AVAILABLE
                ? "Đã có nội dung thuyết minh và âm thanh"
                : "Đã có nội dung thuyết minh, chưa có âm thanh";
        return new NarrationLookupResponse(location.getId(), location.getName(), language.getCode(),
                NarrationAvailability.AVAILABLE, audioStatus, message,
                narrationMapper.toResponse(narration), publishedLanguages);
    }

    @Override
    public List<AvailableLanguageResponse> getAvailableLanguages(Long locationId, NarrationStatus status) {
        if (!locationRepository.existsById(locationId)) {
            throw ResourceNotFoundException.location(locationId);
        }
        return narrationRepository.findAllWithActiveLanguageByLocationId(locationId).stream()
                .filter(n -> status == null || n.getStatus() == status)
                .map(narrationMapper::toAvailableLanguage)
                .toList();
    }

    // =====================================================================
    // TÍCH HỢP DỊCH THUẬT / TEXT-TO-SPEECH
    // =====================================================================

    @Override
    @Transactional
    public NarrationResponse attachAudio(Long id, AttachAudioRequest request) {
        Narration narration = findNarrationWithDetailsOrThrow(id);
        narration.setAudioUrl(request.audioUrl().trim());
        return narrationMapper.toResponse(narrationRepository.saveAndFlush(narration));
    }

    /**
     * Luồng: kiểm tra dữ liệu => gọi TranslationProvider => lưu bản dịch mới (DRAFT).
     *
     * <p>Ghi chú cho bản production: lời gọi AI có thể mất vài giây và đang nằm trong transaction
     * (giữ 1 kết nối DB trong lúc chờ). Với quy mô đồ án điều này chấp nhận được; khi tải lớn nên
     * tách bước gọi AI ra ngoài transaction hoặc xử lý bất đồng bộ (hàng đợi).
     */
    @Override
    @Transactional
    public NarrationResponse translate(Long id, TranslateNarrationRequest request) {
        Narration source = findNarrationWithDetailsOrThrow(id);
        Language target = findActiveLanguageOrThrow(request.targetLanguageCode());
        String sourceCode = source.getLanguage().getCode();

        if (sourceCode.equals(target.getCode())) {
            throw new BusinessException(ErrorCode.SAME_LANGUAGE,
                    "Ngôn ngữ đích trùng với ngôn ngữ của bản thuyết minh gốc");
        }
        ensureNarrationNotExists(source.getLocation(), target);

        TranslationProvider provider = translationProvider.orElseThrow(() -> new IntegrationException(
                ErrorCode.TRANSLATION_PROVIDER_UNAVAILABLE,
                "Chưa cấu hình dịch vụ dịch thuật tự động. Module dịch thuật cần cài đặt interface TranslationProvider"));

        String translatedTitle;
        String translatedContent;
        try {
            translatedTitle = provider.translate(source.getTitle(), sourceCode, target.getCode());
            translatedContent = provider.translate(source.getContent(), sourceCode, target.getCode());
        } catch (RuntimeException ex) {
            throw new IntegrationException(ErrorCode.TRANSLATION_FAILED,
                    "Dịch vụ dịch thuật (" + provider.getName() + ") gặp lỗi, vui lòng thử lại sau", ex);
        }
        if (isBlank(translatedTitle) || isBlank(translatedContent)) {
            throw new IntegrationException(ErrorCode.TRANSLATION_FAILED,
                    "Dịch vụ dịch thuật (" + provider.getName() + ") trả về kết quả rỗng");
        }

        Narration translated = new Narration();
        translated.setLocation(source.getLocation());
        translated.setLanguage(target);
        translated.setTitle(truncate(translatedTitle.trim(), TITLE_MAX_LENGTH));
        translated.setContent(translatedContent.trim());
        translated.setStatus(NarrationStatus.DRAFT);   // luôn chờ người duyệt
        log.info("Đã dịch narration id={} từ {} sang {} bằng provider '{}'",
                id, sourceCode, target.getCode(), provider.getName());

        return narrationMapper.toResponse(narrationRepository.save(translated));
    }

    @Override
    @Transactional
    public NarrationResponse generateAudio(Long id) {
        Narration narration = findNarrationWithDetailsOrThrow(id);

        TextToSpeechProvider provider = textToSpeechProvider.orElseThrow(() -> new IntegrationException(
                ErrorCode.TTS_PROVIDER_UNAVAILABLE,
                "Chưa cấu hình dịch vụ Text-to-Speech. Module TTS cần cài đặt interface TextToSpeechProvider"));

        String audioUrl;
        try {
            audioUrl = provider.synthesize(narration.getContent(), narration.getLanguage().getCode());
        } catch (RuntimeException ex) {
            throw new IntegrationException(ErrorCode.TTS_FAILED,
                    "Dịch vụ Text-to-Speech (" + provider.getName() + ") gặp lỗi, vui lòng thử lại sau", ex);
        }
        if (isBlank(audioUrl)) {
            throw new IntegrationException(ErrorCode.TTS_FAILED,
                    "Dịch vụ Text-to-Speech (" + provider.getName() + ") không trả về URL âm thanh");
        }

        narration.setAudioUrl(audioUrl.trim());
        return narrationMapper.toResponse(narrationRepository.saveAndFlush(narration));
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private Location findLocationOrThrow(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> ResourceNotFoundException.location(locationId));
    }

    private Narration findNarrationWithDetailsOrThrow(Long id) {
        return narrationRepository.findWithDetailsById(id)
                .orElseThrow(() -> ResourceNotFoundException.narration(id));
    }

    /** Ngôn ngữ phải tồn tại (404) và đang được kích hoạt (400 LANGUAGE_INACTIVE). */
    private Language findActiveLanguageOrThrow(String languageCode) {
        String code = Language.normalizeCode(languageCode);
        Language language = languageRepository.findByCode(code)
                .orElseThrow(() -> ResourceNotFoundException.language(code));
        if (!language.isActive()) {
            throw new BusinessException(ErrorCode.LANGUAGE_INACTIVE,
                    "Ngôn ngữ '" + code + "' hiện đang tạm ngừng hỗ trợ");
        }
        return language;
    }

    /** Mỗi địa điểm chỉ có tối đa 1 bản thuyết minh cho mỗi ngôn ngữ. */
    private void ensureNarrationNotExists(Location location, Language language) {
        if (narrationRepository.existsByLocationIdAndLanguageId(location.getId(), language.getId())) {
            throw new DuplicateResourceException(ErrorCode.NARRATION_ALREADY_EXISTS,
                    "Địa điểm '" + location.getName() + "' đã có bản thuyết minh " + language.getName()
                            + " (" + language.getCode() + "). Hãy cập nhật bản hiện có thay vì tạo mới");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
