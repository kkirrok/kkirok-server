package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.dao.CharacterTypeRepository;
import com.kkirok.server.domain.character.domain.CharacterType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterTypeInitializer implements ApplicationRunner {

    private static final String DEFAULT_CHARACTER_TYPE_CODE = "BASIC";
    private static final String DEFAULT_CHARACTER_TYPE_NAME = "기본 캐릭터";
    private static final String DEFAULT_CHARACTER_BASE_IMAGE = "memberInfo-base.png";

    private final CharacterTypeRepository characterTypeRepository;

    @Override
    public void run(final ApplicationArguments args) {
        characterTypeRepository.findByTypeCode(DEFAULT_CHARACTER_TYPE_CODE)
                .orElseGet(() -> characterTypeRepository.save(
                        CharacterType.create(
                                DEFAULT_CHARACTER_TYPE_CODE,
                                DEFAULT_CHARACTER_TYPE_NAME,
                                DEFAULT_CHARACTER_BASE_IMAGE
                        )
                ));
    }
}
