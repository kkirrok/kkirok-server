package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopReaction;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopReactionRepository extends JpaRepository<KkinipopReaction, Long> {

    @Query("""
            select reaction
            from KkinipopReaction reaction
            join KkinipopGroupMember gm
              on gm.group.id = reaction.post.group.id
             and gm.member.id = reaction.member.id
            where reaction.post.id in :postIds
              and gm.banned = false
            """)
    List<KkinipopReaction> findPostReactions(@Param("postIds") Collection<Long> postIds);

    @Query("""
            select r
            from KkinipopReaction r
            where r.post.id = :postId
              and r.member.id = :memberId
              and r.emojiCode = :emojiCode
            """)
    Optional<KkinipopReaction> findByPostAndMemberAndEmojiCode(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId,
            @Param("emojiCode") String emojiCode
    );

    @Query("""
            select r
            from KkinipopReaction r
            where r.post.id = :postId
              and r.member.id = :memberId
              and r.emojiCode <> :emojiCode
            """)
    List<KkinipopReaction> findByPostAndMemberExcludingEmojiCode(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId,
            @Param("emojiCode") String emojiCode
    );

    @Query("""
            select count(r)
            from KkinipopReaction r
            join KkinipopGroupMember gm
              on gm.group.id = r.post.group.id
             and gm.member.id = r.member.id
            where r.post.id = :postId
              and r.emojiCode = :emojiCode
              and gm.banned = false
            """)
    long countByPostAndEmojiCode(
            @Param("postId") Long postId,
            @Param("emojiCode") String emojiCode
    );

}
