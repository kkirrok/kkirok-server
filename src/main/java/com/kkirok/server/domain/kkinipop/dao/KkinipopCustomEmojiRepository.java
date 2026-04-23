package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopCustomEmoji;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopCustomEmojiRepository extends JpaRepository<KkinipopCustomEmoji, Long> {

    @Query("""
            select emoji
            from KkinipopCustomEmoji emoji
            where emoji.group.id = :groupId
              and emoji.deletedAt is null
            order by emoji.createdAt asc
            """)
    List<KkinipopCustomEmoji> findActiveGroupEmojis(@Param("groupId") Long groupId);

    @Query("""
            select count(emoji)
            from KkinipopCustomEmoji emoji
            where emoji.group.id = :groupId
              and emoji.deletedAt is null
            """)
    long countActiveGroupEmojis(@Param("groupId") Long groupId);

    @Query("""
            select emoji
            from KkinipopCustomEmoji emoji
            join fetch emoji.group groupEntity
            join fetch emoji.creator creator
            where emoji.id = :emojiId
              and emoji.deletedAt is null
            """)
    Optional<KkinipopCustomEmoji> findActiveEmoji(@Param("emojiId") Long emojiId);

}
