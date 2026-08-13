package com.kkirok.server.domain.terms.application.service;

import com.kkirok.server.domain.terms.dao.TermsAgreementRepository;
import com.kkirok.server.domain.terms.domain.TermsAgreement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsAgreementSaveService {

    private final TermsAgreementRepository termsAgreementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 내부 실패가 상위에 영향이 가지 않도록 함
    public void saveIgnoringDuplicate(TermsAgreement agreement) {
        try {
            termsAgreementRepository.saveAndFlush(agreement);
        } catch (DataIntegrityViolationException e) {
            log.debug("Skipping duplicated terms agreement for memberId: {}, termsId: {}",
                    agreement.getMember().getId(), agreement.getTerms().getId());
        }
    }
}
