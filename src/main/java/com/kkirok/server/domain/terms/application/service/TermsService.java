package com.kkirok.server.domain.terms.application.service;

import com.kkirok.server.domain.member.application.service.MemberService;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.terms.application.dto.request.TermsAgreeRequest;
import com.kkirok.server.domain.terms.application.dto.response.TermsResponse;
import com.kkirok.server.domain.terms.dao.TermsAgreementRepository;
import com.kkirok.server.domain.terms.dao.TermsRepository;
import com.kkirok.server.domain.terms.domain.Terms;
import com.kkirok.server.domain.terms.domain.TermsAgreement;
import com.kkirok.server.domain.terms.domain.TermsType;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermsService {

    private final TermsRepository termsRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final PresignedUrlService presignedUrlService;
    private final MemberService memberService;
    private final TermsAgreementSaveService termsAgreementSaveService;

    @Transactional(readOnly = true)
    public List<TermsResponse> getAllLatestTerms() {
        return termsRepository.findAllLatest().stream()
                .map(this::toTermsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TermsResponse> getPendingRequiredTerms(Member member) {
        return findPendingRequiredTerms(member.getId()).stream()
                .map(this::toTermsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasPendingRequiredTerms(Long memberId) {
        return !findPendingRequiredTerms(memberId).isEmpty();
    }

    private List<Terms> findPendingRequiredTerms(Long memberId) {
        List<Terms> requiredTerms = termsRepository.findAllLatest().stream()
                .filter(Terms::isRequired)
                .toList();
        if (requiredTerms.isEmpty()) {
            return List.of();
        }

        List<Long> termsIds = requiredTerms.stream().map(Terms::getId).toList();
        Set<Long> agreedTermsIds = termsAgreementRepository
                .findAllByMember_IdAndTerms_IdIn(memberId, termsIds).stream()
                .filter(TermsAgreement::isAgree)
                .map(agreement -> agreement.getTerms().getId())
                .collect(Collectors.toSet());

        return requiredTerms.stream()
                .filter(terms -> !agreedTermsIds.contains(terms.getId()))
                .toList();
    }

    @Transactional
    public void agree(Long memberId, TermsAgreeRequest request) {
        validateDuplicateTermsType(request);
        Member member = memberService.findMemberByMemberId(memberId);
        Map<TermsType, Terms> latestTermsByType = termsRepository.findAllLatest().stream()
                .collect(Collectors.toMap(Terms::getType, Function.identity()));

        for (var item : request.agrees()) {
            Terms terms = latestTermsByType.get(item.type());
            if (terms == null) {
                throw new NotFoundException(TermsErrorCode.TERMS_NOT_FOUND);
            }
            if (terms.isRequired() && !item.isAgree()) {
                throw new BadRequestException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }

            termsAgreementRepository.findByMember_IdAndTerms_Id(memberId, terms.getId())
                    .ifPresentOrElse(
                            agreement -> agreement.update(item.isAgree()),
                            () -> termsAgreementSaveService.saveIgnoringDuplicate(
                                    TermsAgreement.of(member, terms, item.isAgree()))
                    );
        }
    }

    private TermsResponse toTermsResponse(Terms terms) {
        return TermsResponse.of(terms, presignedUrlService.getPresignedUrl(terms.getR2Key()));
    }

    private void validateDuplicateTermsType(TermsAgreeRequest request) {
        Set<TermsType> types = request.agrees().stream()
                .map(TermsAgreeRequest.TermsAgreeItem::type)
                .collect(Collectors.toSet());
        if (types.size() != request.agrees().size()) {
            throw new BadRequestException(TermsErrorCode.TERMS_TYPE_DUPLICATE);
        }
    }
}
