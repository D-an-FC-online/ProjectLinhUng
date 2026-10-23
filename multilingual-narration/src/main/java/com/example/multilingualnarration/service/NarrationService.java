package com.example.multilingualnarration.service;

import com.example.multilingualnarration.dto.request.AttachAudioRequest;
import com.example.multilingualnarration.dto.request.CreateNarrationRequest;
import com.example.multilingualnarration.dto.request.TranslateNarrationRequest;
import com.example.multilingualnarration.dto.request.UpdateNarrationRequest;
import com.example.multilingualnarration.dto.response.AvailableLanguageResponse;
import com.example.multilingualnarration.dto.response.NarrationLookupResponse;
import com.example.multilingualnarration.dto.response.NarrationResponse;
import com.example.multilingualnarration.enums.NarrationStatus;

import java.util.List;

/**
 * Business Logic Layer — nghiệp vụ nội dung thuyết minh, gồm 2 nhóm:
 * <ol>
 *   <li>Quản lý nội dung (CRUD, tra cứu theo ngôn ngữ, liệt kê ngôn ngữ có sẵn).</li>
 *   <li>Điểm tích hợp với module dịch thuật AI và Text-to-Speech (translate, generateAudio, attachAudio).</li>
 * </ol>
 */
public interface NarrationService {

    // ----- Quản lý nội dung -----

    NarrationResponse create(Long locationId, CreateNarrationRequest request);

    NarrationResponse getById(Long id);

    NarrationResponse update(Long id, UpdateNarrationRequest request);

    void delete(Long id);

    /** Tra cứu nội dung cho khách tham quan theo địa điểm + ngôn ngữ, luôn kèm trạng thái rõ ràng. */
    NarrationLookupResponse lookup(Long locationId, String languageCode);

    /**
     * Các ngôn ngữ đã có bản thuyết minh cho địa điểm.
     *
     * @param status null = mọi trạng thái; PUBLISHED = chỉ những bản khách xem được
     */
    List<AvailableLanguageResponse> getAvailableLanguages(Long locationId, NarrationStatus status);

    // ----- Tích hợp dịch thuật / TTS -----

    /** Gắn URL âm thanh do module TTS (chạy như dịch vụ riêng) tạo ra. */
    NarrationResponse attachAudio(Long id, AttachAudioRequest request);

    /** Dịch bản thuyết minh sang ngôn ngữ khác bằng TranslationProvider, tạo bản mới ở trạng thái DRAFT. */
    NarrationResponse translate(Long id, TranslateNarrationRequest request);

    /** Tạo âm thanh cho bản thuyết minh bằng TextToSpeechProvider. */
    NarrationResponse generateAudio(Long id);
}
