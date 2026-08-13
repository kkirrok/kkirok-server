package com.kkirok.server.domain.terms.application.service;

import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.terms.dao.TermsAgreementRepository;
import com.kkirok.server.domain.terms.domain.Terms;
import com.kkirok.server.domain.terms.domain.TermsAgreement;
import com.kkirok.server.domain.terms.domain.TermsType;
import com.kkirok.server.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TermsAgreementSaveServiceTest {

    @Mock
    private TermsAgreementRepository termsAgreementRepository;

    @InjectMocks
    private TermsAgreementSaveService termsAgreementSaveService;

    @Test
    @DisplayName("동의 이력을 저장한다")
    void shouldSave_whenNoConflict() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = Terms.of(TermsType.TERMS_OF_SERVICE, 1, "term/terms-of-service-v1.md");
        ReflectionTestUtils.setField(terms, "id", 1L);
        TermsAgreement agreement = TermsAgreement.of(member, terms, true);

        // When
        termsAgreementSaveService.saveIgnoringDuplicate(agreement);

        // Then
        then(termsAgreementRepository).should().saveAndFlush(agreement);
    }

    @Test
    @DisplayName("동시 요청으로 유니크 제약을 위반해도 예외를 던지지 않는다")
    void shouldNotThrow_whenUniqueConstraintViolated() {
        // Given
        Member member = MemberFixture.createLocalMember("kkirok", "kkirok@test.com");
        ReflectionTestUtils.setField(member, "id", 1L);
        Terms terms = Terms.of(TermsType.TERMS_OF_SERVICE, 1, "term/terms-of-service-v1.md");
        ReflectionTestUtils.setField(terms, "id", 1L);
        TermsAgreement agreement = TermsAgreement.of(member, terms, true);
        given(termsAgreementRepository.saveAndFlush(any())).willThrow(new DataIntegrityViolationException("duplicate"));

        // When, Then
        assertThatCode(() -> termsAgreementSaveService.saveIgnoringDuplicate(agreement))
                .doesNotThrowAnyException();
    }
}
