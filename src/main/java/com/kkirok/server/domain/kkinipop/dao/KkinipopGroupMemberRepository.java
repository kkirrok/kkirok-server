package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopGroupMemberRepository extends JpaRepository<KkinipopGroupMember, Long> {

    @Query("""
            select gm
            from KkinipopGroupMember gm
            join fetch gm.group g
            join fetch gm.member m
            where m.id = :memberId
              and gm.leftAt is null
            order by gm.createdAt asc
            """)
    List<KkinipopGroupMember> findActiveMemberships(@Param("memberId") Long memberId);

    @Query("""
            select gm
            from KkinipopGroupMember gm
            join fetch gm.member m
            where gm.group.id = :groupId
              and gm.leftAt is null
              and m.deletedAt is null
            order by gm.createdAt asc
            """)
    List<KkinipopGroupMember> findActiveGroupMembers(@Param("groupId") Long groupId);

    @Query("""
            select gm
            from KkinipopGroupMember gm
            join fetch gm.group g
            join fetch gm.member m
            where g.id = :groupId
              and m.id = :memberId
              and m.deletedAt is null
              and gm.leftAt is null
            """)
    Optional<KkinipopGroupMember> findActiveMembership(@Param("groupId") Long groupId, @Param("memberId") Long memberId);

    @Query("""
            select count(gm) > 0
            from KkinipopGroupMember gm
            join gm.member m
            where gm.group.id = :groupId
              and gm.member.id = :memberId
              and gm.leftAt is null
              and m.deletedAt is null
            """)
    boolean existsActiveMembership(@Param("groupId") Long groupId, @Param("memberId") Long memberId);

    @Query("""
            select count(gm) > 0
            from KkinipopGroupMember gm
            where gm.group.id = :groupId
              and gm.member.id = :memberId
              and gm.banned = true
            """)
    boolean existsBannedMembership(@Param("groupId") Long groupId, @Param("memberId") Long memberId);

}
