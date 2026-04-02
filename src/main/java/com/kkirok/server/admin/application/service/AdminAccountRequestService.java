package com.kkirok.server.admin.application.service;

import com.kkirok.server.admin.application.dto.request.AdminAccountRequestCreateRequest;
import com.kkirok.server.admin.application.dto.response.AdminAccountRequestResponse;
import com.kkirok.server.admin.application.dto.response.AdminApprovalResponse;
import com.kkirok.server.admin.dao.AdminAccountRequestRepository;
import com.kkirok.server.admin.domain.AdminAccountRequest;
import com.kkirok.server.admin.domain.AdminRequestStatus;
import com.kkirok.server.admin.exception.AdminErrorCode;
import com.kkirok.server.domain.member.application.service.MemberRegistrationService;
import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.common.exception.ConflictException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
	관리자 계정 신청 처리 service

	관리자 계정 요청하면 AdminAccountRequest ( PENDING 상태 ) 저장됨
	목록 조회 후 해당 id에 대해 approve() 하면
	요청 상태가 APPROVED로 변경됨

 */
@Service
@RequiredArgsConstructor
public class AdminAccountRequestService {

	private final AdminAccountRequestRepository adminAccountRequestRepository;
	private final AuthIdentityRepository authIdentityRepository;
	private final MemberRegistrationService memberRegistrationService;
	private final PasswordEncoder passwordEncoder;

	// 요청 데이터(AdminAccountRequest) 생성 및 저장 담당
	@Transactional
	public AdminAccountRequestResponse createRequest(AdminAccountRequestCreateRequest request) {
		validateCreatable(request.email());

		// 요청 데이터 생성 및 저장
		AdminAccountRequest adminAccountRequest = AdminAccountRequest.create(
				request.email(),
				passwordEncoder.encode(request.password()),
				request.name(),
				request.reason()
		);
		adminAccountRequestRepository.save(adminAccountRequest);

		// DTO 생성 및 반환
		return AdminAccountRequestResponse.from(adminAccountRequest);
	}

	// 신청 목록 조회
	@Transactional(readOnly = true)
	public List<AdminAccountRequestResponse> getRequests(AdminRequestStatus status) {
		return adminAccountRequestRepository.findAllByStatusOrderByCreatedAtDesc(status)
				.stream()
				.map(AdminAccountRequestResponse::from)
				.toList();
	}

	// 요청에 데이터 상태 처리 담당
	@Transactional
	public AdminApprovalResponse process(Long requestId, Long approvedByMemberId, AdminRequestStatus status) {

		// 검증
		validateProcessStatus(status);

		AdminAccountRequest request = adminAccountRequestRepository.findById(requestId)
				.orElseThrow(() -> new NotFoundException(AdminErrorCode.ADMIN_ACCOUNT_REQUEST_NOT_FOUND));

		if (request.getStatus() != AdminRequestStatus.PENDING) {
			throw new ConflictException(AdminErrorCode.ADMIN_ACCOUNT_REQUEST_NOT_PENDING);
		}

		if (status == AdminRequestStatus.REJECTED) {
			request.reject();
			return AdminApprovalResponse.of(request.getId(), status, null, request.getEmail(), null);
		}

		if (authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, request.getEmail())) {
			throw new ConflictException(AdminErrorCode.ADMIN_ACCOUNT_EMAIL_ALREADY_EXISTS);
		}

		// 생성•저장
		Member adminMember = memberRegistrationService.registerAdminMember(
				request.getEmail(),
				request.getPasswordHash(),
				request.getName()
		);
		request.approve(approvedByMemberId);

		// 반환
		return AdminApprovalResponse.of(request.getId(), status, adminMember.getId(), request.getEmail(), Role.ADMIN);
	}

	// 검증 - email 검사
	private void validateCreatable(String email) {
		if (!email.endsWith("@kkirok.com")) {
			throw new BadRequestException(AdminErrorCode.ADMIN_EMAIL_DOMAIN_INVALID);
		}
		if (adminAccountRequestRepository.existsByEmailAndStatus(email, AdminRequestStatus.PENDING)) {
			throw new ConflictException(AdminErrorCode.ADMIN_ACCOUNT_REQUEST_ALREADY_EXISTS);
		}
		if (authIdentityRepository.existsByProviderAndProviderUserId(AuthProvider.LOCAL, email)) {
			throw new ConflictException(AdminErrorCode.ADMIN_ACCOUNT_EMAIL_ALREADY_EXISTS);
		}
	}

	// 검증 - 상태 검사
	private void validateProcessStatus(AdminRequestStatus status) {
		if (status == AdminRequestStatus.PENDING) {
			throw new BadRequestException(AdminErrorCode.ADMIN_ACCOUNT_REQUEST_STATUS_INVALID);
		}
	}
}
