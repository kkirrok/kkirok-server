package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopPost;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopPostRepository extends JpaRepository<KkinipopPost, Long> {

    @Query("""
            select count(post)
            from KkinipopPost post
            where post.member.id = :memberId
              and post.mission.id = :missionId
              and post.recordDate = :recordDate
              and post.deletedAt is null
            """)
    long countMissionPosts(
            @Param("memberId") Long memberId,
            @Param("missionId") Long missionId,
            @Param("recordDate") LocalDate recordDate
    );

    @Query("""
            select count(post)
            from KkinipopPost post
            where post.group.id = :groupId
              and post.member.id = :memberId
              and post.recordDate = :recordDate
              and post.saveToPersonalLog = true
              and post.deletedAt is null
            """)
    long countMyKkirokSavedPosts(
            @Param("groupId") Long groupId,
            @Param("memberId") Long memberId,
            @Param("recordDate") LocalDate recordDate
    );

    @Query("""
            select post
            from KkinipopPost post
            join fetch post.member member
            left join fetch post.mission mission
            join KkinipopGroupMember gm
              on gm.group.id = post.group.id
             and gm.member.id = post.member.id
            where post.group.id = :groupId
              and post.recordDate = :recordDate
              and post.deletedAt is null
              and gm.banned = false
            order by post.createdAt desc
            """)
    List<KkinipopPost> findDailyPosts(@Param("groupId") Long groupId, @Param("recordDate") LocalDate recordDate);

    @Query("""
            select post
            from KkinipopPost post
            join fetch post.member member
            left join fetch post.mission mission
            where post.group.id = :groupId
            """)
    List<KkinipopPost> findGroupPosts(@Param("groupId") Long groupId);

    @Query("""
            select p
            from KkinipopPost p
            join fetch p.member member
            left join fetch p.mission mission
            join KkinipopGroupMember gm
              on gm.group.id = p.group.id
             and gm.member.id = p.member.id
            where p.group.id = :groupId
              and p.recordDate between :startDate and :endDate
              and (:missionId is null or mission.id = :missionId)
              and p.deletedAt is null
              and gm.banned = false
            order by p.recordDate desc, p.createdAt desc
            """)
    List<KkinipopPost> findPostsInDateRange(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("missionId") Long missionId
    );
}
