package com.example.multilingualnarration.mapper;

import com.example.multilingualnarration.dto.response.LanguageResponse;
import com.example.multilingualnarration.entity.Language;
import org.springframework.stereotype.Component;

/**
 * Chuyển Entity {@link Language} sang DTO trả về cho client.
 */
@Component
public class LanguageMapper {

    public LanguageResponse toResponse(Language language) {
        return new LanguageResponse(
                language.getCode(),
                language.getName(),
                language.getNativeName(),
                language.isActive());
    }
}
