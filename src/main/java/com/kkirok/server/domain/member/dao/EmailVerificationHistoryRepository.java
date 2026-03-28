package com.kkirok.server.domain.member.dao;

import com.kkirok.server.domain.member.domain.EmailVerificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationHistoryRepository extends JpaRepository<EmailVerificationHistory, Long> {

    @Query("""
            SELECT evh
            FROM EmailVerificationHistory evh
            WHERE evh.email = :email
              AND evh.code = :code
              AND evh.isSuccess = false
            ORDER BY evh.requestAt DESC
            """)
    List<EmailVerificationHistory> findPendingHistories(
            @Param("email") String email,
            @Param("code") String code,
            Pageable pageable
    );

    default Optional<EmailVerificationHistory> findLatestPendingHistory(final String email, final String code) {
        return findPendingHistories(email, code, Pageable.ofSize(1)).stream().findFirst();
    }
}
