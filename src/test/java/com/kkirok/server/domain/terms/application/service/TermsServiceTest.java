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
import com.kkirok.server.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock
    private TermsRepository termsRepository;

    @Mock
    private TermsAgreementRepository termsAgreementRepository;

    @Mock
    private PresignedUrlService presignedUrlService;

    @Mock
    private MemberService memberService;

    @Mock
    private TermsAgreementSaveService termsAgreementSaveService;

    @InjectMocks
    private TermsService termsService;

    private Terms termsOfService(int version) {
        Terms terms = Terms.of(TermsType.TERMS_OF_SERVICE, version, "term/terms-of-service-v" + version + ".md");
        ReflectionTestUtils.setField(terms, "id", (long) version);
        return terms;
    }

    private Terms marketing(int version) {
        Terms terms = Terms.of(TermsType.MARKETING, version, "term/marketing-v" + version + ".md");
        ReflectionTestUtils.setField(terms, "id", 100L + version);
        return terms;
    }

    private URL url(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("최신 약관 목록을 조회하면 타입별 최신 버전과 다운로드 URL을 반환한다")
    void shouldReturnLatestTermsWithUrl_whenGetAllLatestTerms() {
        // Given
        Terms terms = termsOfService(2);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms));
        given(presignedUrlService.getPresignedUrl(terms.getR2Key())).willReturn(url("https://cdn.test/term/terms-of-service-v2.md"));

        // When
        List<TermsResponse> responses = termsService.getAllLatestTerms();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo(TermsType.TERMS_OF_SERVICE);
        assertThat(responses.get(0).isRequired()).isTrue();
        assertThat(responses.get(0).version()).isEqualTo(2);
        assertThat(responses.get(0).url()).isEqualTo("https://cdn.test/term/terms-of-service-v2.md");
    }

    @Test
    @DisplayName("필수 약관에 동의한 적이 없으면 재동의 필요 목록에 포함된다")
    void shouldIncludeInPending_whenMemberNeverAgreedToRequiredTerms() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = termsOfService(1);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms));
        given(termsAgreementRepository.findAllByMember_IdAndTerms_IdIn(1L, List.of(terms.getId())))
                .willReturn(List.of());
        given(presignedUrlService.getPresignedUrl(anyString())).willReturn(url("https://cdn.test/x"));

        // When
        List<TermsResponse> pending = termsService.getPendingRequiredTerms(member);

        // Then
        assertThat(pending).extracting(TermsResponse::type).containsExactly(TermsType.TERMS_OF_SERVICE);
    }

    @Test
    @DisplayName("필수 약관 최신 버전에 이미 동의했으면 재동의 필요 목록에서 제외된다")
    void shouldExcludeFromPending_whenMemberAlreadyAgreedToLatestRequiredTerms() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = termsOfService(1);
        TermsAgreement agreement = TermsAgreement.of(member, terms, true);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms));
        given(termsAgreementRepository.findAllByMember_IdAndTerms_IdIn(1L, List.of(terms.getId())))
                .willReturn(List.of(agreement));

        // When
        List<TermsResponse> pending = termsService.getPendingRequiredTerms(member);

        // Then
        assertThat(pending).isEmpty();
        then(presignedUrlService).should(never()).getPresignedUrl(anyString());
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않은 상태면 미동의 필수 약관이 있다")
    void shouldReturnTrue_whenMemberHasNotAgreedToRequiredTerms() {
        // Given
        Terms terms = termsOfService(1);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms));
        given(termsAgreementRepository.findAllByMember_IdAndTerms_IdIn(1L, List.of(terms.getId())))
                .willReturn(List.of());

        // When
        boolean hasPendingRequiredTerms = termsService.hasPendingRequiredTerms(1L);

        // Then
        assertThat(hasPendingRequiredTerms).isTrue();
    }

    @Test
    @DisplayName("필수 약관 최신 버전에 모두 동의했으면 미동의 필수 약관이 없고 다운로드 URL을 생성하지 않는다")
    void shouldReturnFalseWithoutPresignedUrl_whenMemberAgreedToAllLatestRequiredTerms() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = termsOfService(1);
        TermsAgreement agreement = TermsAgreement.of(member, terms, true);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms));
        given(termsAgreementRepository.findAllByMember_IdAndTerms_IdIn(1L, List.of(terms.getId())))
                .willReturn(List.of(agreement));

        // When
        boolean hasPendingRequiredTerms = termsService.hasPendingRequiredTerms(1L);

        // Then
        assertThat(hasPendingRequiredTerms).isFalse();
        then(presignedUrlService).should(never()).getPresignedUrl(any());
    }

    @Test
    @DisplayName("약관이 개정되어 새 버전이 생기면 과거 버전 동의로는 재동의 필요 목록에서 빠지지 않는다")
    void shouldStayPending_whenMemberOnlyAgreedToOldVersion() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms latest = termsOfService(2);
        given(termsRepository.findAllLatest()).willReturn(List.of(latest));
        given(termsAgreementRepository.findAllByMember_IdAndTerms_IdIn(1L, List.of(latest.getId())))
                .willReturn(List.of());
        given(presignedUrlService.getPresignedUrl(anyString())).willReturn(url("https://cdn.test/x"));

        // When
        List<TermsResponse> pending = termsService.getPendingRequiredTerms(member);

        // Then
        assertThat(pending).extracting(TermsResponse::version).containsExactly(2);
    }

    @Test
    @DisplayName("필수 약관과 선택 약관에 동의하면 각각 동의 이력이 저장된다")
    void shouldSaveAgreement_whenAgreeToRequiredAndOptionalTerms() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = termsOfService(1);
        Terms marketing = marketing(1);
        TermsAgreeRequest request = new TermsAgreeRequest(List.of(
                new TermsAgreeRequest.TermsAgreeItem(TermsType.TERMS_OF_SERVICE, true),
                new TermsAgreeRequest.TermsAgreeItem(TermsType.MARKETING, false)
        ));
        given(memberService.findMemberByMemberId(1L)).willReturn(member);
        given(termsRepository.findAllLatest()).willReturn(List.of(terms, marketing));
        given(termsAgreementRepository.findByMember_IdAndTerms_Id(any(), any())).willReturn(Optional.empty());

        // When
        termsService.agree(1L, request);

        // Then
        then(termsAgreementSaveService).should().saveIgnoringDuplicate(argThatAgree(TermsType.TERMS_OF_SERVICE, true));
        then(termsAgreementSaveService).should().saveIgnoringDuplicate(argThatAgree(TermsType.MARKETING, false));
    }

    private TermsAgreement argThatAgree(TermsType type, boolean isAgree) {
        return org.mockito.ArgumentMatchers.argThat(agreement ->
                agreement.getTerms().getType() == type && agreement.isAgree() == isAgree);
    }

    @Test
    @DisplayName("이미 동의 이력이 있는 약관에 다시 동의하면 기존 이력을 갱신한다")
    void shouldUpdateExistingAgreement_whenAgreeAgain() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms marketing = marketing(1);
        TermsAgreement existing = TermsAgreement.of(member, marketing, false);
        TermsAgreeRequest request = new TermsAgreeRequest(List.of(
                new TermsAgreeRequest.TermsAgreeItem(TermsType.MARKETING, true)
        ));
        given(memberService.findMemberByMemberId(1L)).willReturn(member);
        given(termsRepository.findAllLatest()).willReturn(List.of(marketing));
        given(termsAgreementRepository.findByMember_IdAndTerms_Id(1L, marketing.getId())).willReturn(Optional.of(existing));

        // When
        termsService.agree(1L, request);

        // Then
        assertThat(existing.isAgree()).isTrue();
        then(termsAgreementSaveService).should(never()).saveIgnoringDuplicate(any());
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 예외가 발생한다")
    void shouldThrowBadRequestException_whenRequiredTermsNotAgreed() {
        // Given
        TermsAgreeRequest request = new TermsAgreeRequest(List.of(
                new TermsAgreeRequest.TermsAgreeItem(TermsType.TERMS_OF_SERVICE, false)
        ));
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        given(memberService.findMemberByMemberId(1L)).willReturn(member);
        given(termsRepository.findAllLatest()).willReturn(List.of(termsOfService(1)));

        // When, Then
        assertThatThrownBy(() -> termsService.agree(1L, request))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
    }

    @Test
    @DisplayName("존재하지 않는 약관 유형에 동의하려 하면 예외가 발생한다")
    void shouldThrowNotFoundException_whenTermsTypeDoesNotExist() {
        // Given
        TermsAgreeRequest request = new TermsAgreeRequest(List.of(
                new TermsAgreeRequest.TermsAgreeItem(TermsType.MARKETING, true)
        ));
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        given(memberService.findMemberByMemberId(1L)).willReturn(member);
        given(termsRepository.findAllLatest()).willReturn(List.of());

        // When, Then
        assertThatThrownBy(() -> termsService.agree(1L, request))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TermsErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    @DisplayName("요청에 같은 약관 유형이 중복되면 예외가 발생한다")
    void shouldThrowBadRequestException_whenRequestHasDuplicateType() {
        // Given
        TermsAgreeRequest request = new TermsAgreeRequest(List.of(
                new TermsAgreeRequest.TermsAgreeItem(TermsType.MARKETING, true),
                new TermsAgreeRequest.TermsAgreeItem(TermsType.MARKETING, false)
        ));

        // When, Then
        assertThatThrownBy(() -> termsService.agree(1L, request))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(TermsErrorCode.TERMS_TYPE_DUPLICATE);
        then(memberService).should(never()).findMemberByMemberId(any());
    }
}
