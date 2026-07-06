package com.kkirok.server.domain.kkinipop.dao;

import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KkinipopMissionRepository extends JpaRepository<KkinipopMission, Long> {

    @Query("""
            select mission
            from KkinipopMission mission
            where mission.group.id = :groupId
              and mission.startAt >= :startAt
              and mission.startAt < :endAt
            order by mission.startAt asc
            """)
    List<KkinipopMission> findMissionsByDate(
            @Param("groupId") Long groupId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            select mission
            from KkinipopMission mission
            where mission.group.id = :groupId
              and mission.id = :missionId
              and mission.startAt >= :startAt
              and mission.startAt < :endAt
            """)
    Optional<KkinipopMission> findTodayMission(
            @Param("groupId") Long groupId,
            @Param("missionId") Long missionId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            select mission
            from KkinipopMission mission
            where mission.group.id = :groupId
              and mission.startAt <= :now
              and mission.endAt > :now
              and mission.closedAt is null
            order by mission.startAt desc
            """)
    List<KkinipopMission> findLiveMissions(@Param("groupId") Long groupId, @Param("now") LocalDateTime now);

    @Query("""
            select mission
            from KkinipopMission mission
            where mission.startAt >= :from
              and mission.startAt < :to
              and mission.closedAt is null
            order by mission.startAt asc
            """)
    List<KkinipopMission> findStartingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select mission
            from KkinipopMission mission
            where mission.startAt >= :from
              and mission.startAt < :to
            order by mission.startAt asc
            """)
    List<KkinipopMission> findMissionsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
