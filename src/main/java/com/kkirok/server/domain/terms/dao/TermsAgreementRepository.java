package com.kkirok.server.domain.terms.dao;

import com.kkirok.server.domain.terms.domain.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {

    List<TermsAgreement> findAllByMember_IdAndTerms_IdIn(Long memberId, List<Long> termsIds);

    Optional<TermsAgreement> findByMember_IdAndTerms_Id(Long memberId, Long termsId);
}
