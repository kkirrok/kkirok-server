package com.kkirok.server.domain.notification.dao;

import com.kkirok.server.domain.notification.domain.Notification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByMember_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Optional<Notification> findByIdAndMember_Id(Long id, Long memberId);

    long countByMember_IdAndReadAtIsNull(Long memberId);

    @Modifying
    @Query("""
            update Notification notification
            set notification.readAt = :now
            where notification.id = :id
              and notification.member.id = :memberId
            """)
    int markRead(@Param("id") Long id, @Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            update Notification notification
            set notification.readAt = :now
            where notification.member.id = :memberId
            """)
    int markAllRead(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}
