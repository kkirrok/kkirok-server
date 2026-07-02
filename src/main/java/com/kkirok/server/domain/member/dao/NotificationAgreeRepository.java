package com.kkirok.server.domain.member.dao;

import com.kkirok.server.domain.member.domain.NotificationAgree;
import com.kkirok.server.domain.member.domain.NotificationAgreeId;
import com.kkirok.server.domain.member.domain.NotificationAgreeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface NotificationAgreeRepository extends JpaRepository<NotificationAgree, NotificationAgreeId> {

    List<NotificationAgree> findAllByMember_Id(Long memberId);

    @Query("SELECT na.member.id FROM NotificationAgree na WHERE na.member.id IN :memberIds AND na.id.type = :type AND na.isAgree = false")
    Set<Long> findOptedOutMemberIds(@Param("memberIds") Set<Long> memberIds, @Param("type") NotificationAgreeType type);
}
