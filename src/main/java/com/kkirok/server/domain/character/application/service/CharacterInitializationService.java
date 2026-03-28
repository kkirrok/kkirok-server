package com.kkirok.server.domain.character.application.service;

import com.kkirok.server.domain.character.dao.CharacterRepository;
import com.kkirok.server.domain.character.dao.CharacterTypeRepository;
import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.CharacterType;
import com.kkirok.server.domain.character.exception.CharacterErrorCode;
import com.kkirok.server.domain.character.exception.CharacterException;
import com.kkirok.server.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterInitializationService {

    private static final String DEFAULT_CHARACTER_TYPE_CODE = "BASIC";

    private final CharacterRepository characterRepository;
    private final CharacterTypeRepository characterTypeRepository;

    @Transactional
    public void createInitialCharacter(final Member member) {
        CharacterType characterType = characterTypeRepository.findByTypeCode(DEFAULT_CHARACTER_TYPE_CODE)
                .orElseThrow(() -> new CharacterException(CharacterErrorCode.CHARACTER_TYPE_NOT_FOUND));

        characterRepository.save(Character.create(member, characterType));
    }
}
