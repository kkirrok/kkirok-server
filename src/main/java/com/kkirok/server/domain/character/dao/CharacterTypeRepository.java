package com.kkirok.server.domain.character.dao;

import com.kkirok.server.domain.character.domain.CharacterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterTypeRepository extends JpaRepository<CharacterType, Long> {

    Optional<CharacterType> findByTypeCode(String typeCode);
}
