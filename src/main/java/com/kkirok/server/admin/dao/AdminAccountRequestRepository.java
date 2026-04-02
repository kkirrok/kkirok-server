package com.kkirok.server.admin.dao;

import com.kkirok.server.admin.domain.AdminAccountRequest;
import com.kkirok.server.admin.domain.AdminRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminAccountRequestRepository extends JpaRepository<AdminAccountRequest, Long> {

	boolean existsByEmailAndStatus(String email, AdminRequestStatus status);

	List<AdminAccountRequest> findAllByStatusOrderByCreatedAtDesc(AdminRequestStatus status);
}
