package com.kkirok.server.domain.character.dao;

import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.domain.ItemPossession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    @Query("""
        SELECT c FROM Character c
        WHERE c.member.id = :memberId
    """)
    Optional<Character> findDefaultInfo(@Param("memberId") Long memberId);

    @Query("""
        SELECT DISTINCT c FROM Character c
        LEFT JOIN FETCH c.itemPossessions ip
        LEFT JOIN FETCH ip.item
        WHERE c.member.id = :memberId
    """)
    Optional<Character> findCharacterFetchItems(@Param("memberId") Long memberId);

    @Query("""
        SELECT ip FROM Character c
        JOIN c.itemPossessions ip
        JOIN FETCH ip.item i
        WHERE c.member.id = :memberId
          AND i.id = :itemId
    """)
    Optional<ItemPossession> findItemPossessionFetchItems(@Param("memberId") Long memberId, @Param("itemId") Long itemId);

}
