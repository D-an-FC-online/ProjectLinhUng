package com.example.multilingualnarration.service.impl;

import com.example.multilingualnarration.dto.request.CreateLanguageRequest;
import com.example.multilingualnarration.dto.request.UpdateLanguageRequest;
import com.example.multilingualnarration.dto.response.LanguageResponse;
import com.example.multilingualnarration.entity.Language;
import com.example.multilingualnarration.exception.DuplicateResourceException;
import com.example.multilingualnarration.exception.ErrorCode;
import com.example.multilingualnarration.exception.ResourceNotFoundException;
import com.example.multilingualnarration.mapper.LanguageMapper;
import com.example.multilingualnarration.repository.LanguageRepository;
import com.example.multilingualnarration.service.LanguageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cài đặt nghiệp vụ ngôn ngữ.
 */
@Service
@Transactional(readOnly = true)
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    public LanguageServiceImpl(LanguageRepository languageRepository, LanguageMapper languageMapper) {
        this.languageRepository = languageRepository;
        this.languageMapper = languageMapper;
    }

    @Override
    public List<LanguageResponse> getAll(boolean includeInactive) {
        List<Language> languages = includeInactive
                ? languageRepository.findAllByOrderByIdAsc()
                : languageRepository.findByActiveTrueOrderByIdAsc();
        return languages.stream().map(languageMapper::toResponse).toList();
    }

    @Override
    public LanguageResponse getByCode(String code) {
        return languageMapper.toResponse(findByCodeOrThrow(code));
    }

    /** Quy tắc: mã ngôn ngữ là duy nhất (không phân biệt hoa thường) => trùng thì trả 409. */
    @Override
    @Transactional
    public LanguageResponse create(CreateLanguageRequest request) {
        String code = Language.normalizeCode(request.code());
        if (languageRepository.existsByCode(code)) {
            throw new DuplicateResourceException(ErrorCode.LANGUAGE_ALREADY_EXISTS,
                    "Mã ngôn ngữ '" + code + "' đã tồn tại");
        }
        Language language = new Language();
        language.setCode(code);
        language.setName(request.name().trim());
        language.setNativeName(trimToNull(request.nativeName()));
        language.setActive(request.active() == null || request.active());
        return languageMapper.toResponse(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponse update(String code, UpdateLanguageRequest request) {
        Language language = findByCodeOrThrow(code);
        language.setName(request.name().trim());
        language.setNativeName(trimToNull(request.nativeName()));
        language.setActive(request.active());
        return languageMapper.toResponse(languageRepository.save(language));
    }

    private Language findByCodeOrThrow(String code) {
        String normalized = Language.normalizeCode(code);
        return languageRepository.findByCode(normalized)
                .orElseThrow(() -> ResourceNotFoundException.language(normalized));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
