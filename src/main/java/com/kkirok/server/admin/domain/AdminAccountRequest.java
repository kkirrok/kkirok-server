package com.kkirok.server.admin.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "admin_account_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAccountRequest extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(nullable = false, length = 30)
	private String name;

	@Column(nullable = false, length = 500)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AdminRequestStatus status;

	@Column(name = "approved_by_member_id")
	private Long approvedByMemberId;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "rejected_at")
	private LocalDateTime rejectedAt;

	@Column(name = "rejected_reason", length = 500)
	private String rejectedReason;

	@Builder
	private AdminAccountRequest(
			String email,
			String passwordHash,
			String name,
			String reason,
			AdminRequestStatus status,
			Long approvedByMemberId,
			LocalDateTime approvedAt,
			LocalDateTime rejectedAt,
			String rejectedReason
	) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.name = name;
		this.reason = reason;
		this.status = status;
		this.approvedByMemberId = approvedByMemberId;
		this.approvedAt = approvedAt;
		this.rejectedAt = rejectedAt;
		this.rejectedReason = rejectedReason;
	}

	public static AdminAccountRequest create(String email, String passwordHash, String name, String reason) {
		return AdminAccountRequest.builder()
				.email(email)
				.passwordHash(passwordHash)
				.name(name)
				.reason(reason)
				.status(AdminRequestStatus.PENDING)
				.build();
	}

	public void approve(Long approvedByMemberId) {
		this.status = AdminRequestStatus.APPROVED;
		this.approvedByMemberId = approvedByMemberId;
		this.approvedAt = LocalDateTime.now();
		this.rejectedAt = null;
		this.rejectedReason = null;
	}

	public void reject() {
		this.status = AdminRequestStatus.REJECTED;
		this.approvedByMemberId = null;
		this.approvedAt = null;
		this.rejectedAt = LocalDateTime.now();
		this.rejectedReason = null;
	}
}
