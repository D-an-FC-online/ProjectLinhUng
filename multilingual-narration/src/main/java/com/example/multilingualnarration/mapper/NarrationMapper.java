package com.example.multilingualnarration.mapper;

import com.example.multilingualnarration.dto.response.AvailableLanguageResponse;
import com.example.multilingualnarration.dto.response.NarrationResponse;
import com.example.multilingualnarration.entity.Language;
import com.example.multilingualnarration.entity.Location;
import com.example.multilingualnarration.entity.Narration;
import org.springframework.stereotype.Component;

/**
 * Chuyển Entity {@link Narration} sang các DTO trả về.
 *
 * <p>Lưu ý: method này truy cập narration.getLocation() và narration.getLanguage() (quan hệ LAZY),
 * nên chỉ được gọi bên trong transaction của Service, với Narration đã được nạp kèm quan hệ
 * (qua @EntityGraph / join fetch) để không phát sinh truy vấn N+1.
 */
@Component
public class NarrationMapper {

    public NarrationResponse toResponse(Narration narration) {
        Location location = narration.getLocation();
        Language language = narration.getLanguage();
        return new NarrationResponse(
                narration.getId(),
                location.getId(),
                location.getName(),
                language.getCode(),
                language.getName(),
                narration.getTitle(),
                narration.getContent(),
                narration.getAudioUrl(),
                narration.getAudioStatus(),
                narration.getStatus(),
                DateTimeMapper.toOffsetDateTime(narration.getCreatedAt()),
                DateTimeMapper.toOffsetDateTime(narration.getUpdatedAt()));
    }

    public AvailableLanguageResponse toAvailableLanguage(Narration narration) {
        Language language = narration.getLanguage();
        return new AvailableLanguageResponse(
                language.getCode(),
                language.getName(),
                language.getNativeName(),
                narration.getId(),
                narration.getStatus(),
                narration.getAudioStatus());
    }
}
