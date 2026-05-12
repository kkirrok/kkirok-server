package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopReaction;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopReactionRepository extends JpaRepository<KkinipopReaction, Long> {

    @Query("""
            select reaction
            from KkinipopReaction reaction
            where reaction.post.id in :postIds
            """)
    List<KkinipopReaction> findPostReactions(@Param("postIds") Collection<Long> postIds);

    @Query("""
            select count(r) > 0
            from KkinipopReaction r
            where r.post.id = :postId
              and r.member.id = :memberId
              and r.emojiCode = :emojiCode
            """)
    boolean existsByPostAndMemberAndEmojiCode(
            @Param("postId") Long postId,
            @Param("memberId") Long memberId,
            @Param("emojiCode") String emojiCode
    );

}
