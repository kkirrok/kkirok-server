package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.dto.response.OnboardingProfileResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.application.service.UserRoleService;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import com.kkirok.server.support.fixture.MemberFixture;
import com.kkirok.server.support.fixture.ProfileSettingRequestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private MemberUseCase memberUseCase;

    @Mock
    private R2UploadService r2UploadService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private MealStyleClassificationService mealStyleClassificationService;

    @InjectMocks
    private OnboardingService onboardingService;

    @Test
    @DisplayName("프로필 설정 요청이 올바르면 회원 정보와 온보딩 정보를 함께 저장한다")
    void shouldSaveProfileAndOnboarding_whenRequestIsValid() {
        // Given
        Long memberId = 1L;
        ProfileSettingRequest request = ProfileSettingRequestFixture.create();
        Member member = MemberFixture.createLocalMember();
        MultipartFile profileImage = createProfileImage();

        given(memberUseCase.findMemberByMemberId(memberId)).willReturn(member);
        given(r2UploadService.upload(profileImage)).willReturn("profile-key");

        // When
        onboardingService.updateProfile(memberId, request, profileImage);

        // Then
        assertThat(member.isOnboardingCompleted()).isTrue();
        assertThat(member.getName()).isEqualTo(request.name());
        assertThat(member.getNickname()).isEqualTo(request.nickname());
        assertThat(member.getPhone()).isEqualTo(request.phone());
        assertThat(member.getGender()).isEqualTo(request.gender());
        assertThat(member.getBirthday()).isEqualTo(request.birth());
        assertThat(member.getProfileImage()).isEqualTo("profile-key");
        assertThat(member.getOnboarding()).isNotNull();
        assertThat(member.getOnboarding().getMember()).isSameAs(member);
        assertThat(member.getOnboarding().getPurpose()).isEqualTo(request.purpose());
        assertThat(member.getOnboarding().getHabits()).containsExactlyElementsOf(request.habits());
        assertThat(member.getMealStyle()).isNull();

        then(memberUseCase).should().findMemberByMemberId(memberId);
        then(r2UploadService).should().upload(profileImage);
        then(userRoleService).should().promoteToUser(member.getUser());
        then(memberUseCase).should().updateMember(member);
    }

    @Test
    @DisplayName("식습관은 5개를 초과해 저장할 수 없다")
    void shouldThrowBadRequestException_whenHabitCountExceedsFive() {
        // Given
        ProfileSettingRequest request = ProfileSettingRequestFixture.create(
                List.of(
                        OnboardingHabit.MEAT,
                        OnboardingHabit.SNACK,
                        OnboardingHabit.DIET,
                        OnboardingHabit.REGULAR_MEAL,
                        OnboardingHabit.SPICY,
                        OnboardingHabit.SWEET
                )
        );
        MultipartFile profileImage = createProfileImage();

        // When, Then
        assertThatThrownBy(() -> onboardingService.updateProfile(1L, request, profileImage))
                .isInstanceOf(BadRequestException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.ONBOARDING_HABIT_MAX_COUNT);
    }

    @Test
    @DisplayName("식습관을 선택하지 않으면 빈 리스트로 저장한다")
    void shouldSaveEmptyHabitList_whenHabitsIsNull() {
        // Given
        Long memberId = 1L;
        ProfileSettingRequest request = ProfileSettingRequestFixture.create(null, null);
        Member member = MemberFixture.createLocalMember();
        MultipartFile profileImage = createProfileImage();

        given(memberUseCase.findMemberByMemberId(memberId)).willReturn(member);
        given(r2UploadService.upload(profileImage)).willReturn("profile-key");

        // When
        onboardingService.updateProfile(memberId, request, profileImage);

        // Then
        assertThat(member.getOnboarding()).isNotNull();
        assertThat(member.getOnboarding().getPurpose()).isNull();
        assertThat(member.getOnboarding().getHabits()).isEmpty();
        assertThat(member.getMealStyle()).isNull();
        then(memberUseCase).should().updateMember(member);
    }

    @Test
    @DisplayName("assignMealStyle 호출 시 classify 결과를 MemberUseCase에 전달한다")
    void shouldCallUpdateMealStyle_whenAssignMealStyleIsCalled() {
        // Given
        Long memberId = 1L;
        List<OnboardingHabit> habits = List.of(OnboardingHabit.MEAT, OnboardingHabit.LATE_NIGHT_MEAL);

        given(mealStyleClassificationService.classify(habits)).willReturn(MealStyle.LATE_NIGHT);

        // When
        onboardingService.assignMealStyle(memberId, habits);

        // Then
        then(mealStyleClassificationService).should().classify(habits);
        then(memberUseCase).should().updateMealStyle(memberId, MealStyle.LATE_NIGHT);
    }

    @Test
    @DisplayName("프로필 사진이 없으면 업로드 없이 프로필 설정을 저장한다")
    void shouldSaveProfileWithoutUploadingImage_whenProfileImageIsMissing() {
        // Given
        Long memberId = 1L;
        ProfileSettingRequest request = ProfileSettingRequestFixture.create();
        Member member = MemberFixture.createLocalMember();

        given(memberUseCase.findMemberByMemberId(memberId)).willReturn(member);

        // When
        onboardingService.updateProfile(memberId, request, null);

        // Then
        assertThat(member.getProfileImage()).isNull();
        then(memberUseCase).should().findMemberByMemberId(memberId);
        then(memberUseCase).should().updateMember(member);
    }

    @Test
    @DisplayName("온보딩 정보를 조회하면 전체 선택지와 현재 선택 상태를 함께 반환한다")
    void shouldReturnOnboardingProfileResponse_whenOnboardingExists() {
        // Given
        Long memberId = 1L;
        Member member = MemberFixture.createLocalMember();
        ProfileSettingRequest request = ProfileSettingRequestFixture.create(
                OnboardingPurpose.HABIT,
                List.of(OnboardingHabit.MEAT, OnboardingHabit.SNACK)
        );
        member.updateOnboarding(request, "profile-key");

        given(memberUseCase.findWithOnboarding(memberId)).willReturn(member);

        // When
        OnboardingProfileResponse response = onboardingService.getOnboardingInfo(memberId);

        // Then
        assertThat(response.nickname()).isEqualTo(member.getNickname());
        assertThat(response.gender()).isEqualTo(member.getGender());
        assertThat(response.profileImage()).isEqualTo(member.getProfileImage());
        assertThat(response.purposes())
                .filteredOn(OnboardingProfileResponse.OnboardingLabelInfo::isSelected)
                .extracting(OnboardingProfileResponse.OnboardingLabelInfo::label)
                .containsExactly(OnboardingPurpose.HABIT.getLabel());
        assertThat(response.habits())
                .filteredOn(OnboardingProfileResponse.OnboardingLabelInfo::isSelected)
                .extracting(OnboardingProfileResponse.OnboardingLabelInfo::label)
                .containsExactlyInAnyOrder(
                        OnboardingHabit.MEAT.getLabel(),
                        OnboardingHabit.SNACK.getLabel()
                );
    }

    @Test
    @DisplayName("온보딩 정보가 없으면 전체 선택지를 미선택 상태로 반환한다")
    void shouldReturnUnselectedOnboardingChoices_whenOnboardingDoesNotExist() {
        // Given
        Long memberId = 1L;
        Member member = MemberFixture.createLocalMember();

        given(memberUseCase.findWithOnboarding(memberId)).willReturn(member);

        // When
        OnboardingProfileResponse response = onboardingService.getOnboardingInfo(memberId);

        // Then
        assertThat(response.purposes()).hasSize(OnboardingPurpose.values().length);
        assertThat(response.habits()).hasSize(OnboardingHabit.values().length);
        assertThat(response.purposes()).allMatch(info -> !info.isSelected());
        assertThat(response.habits()).allMatch(info -> !info.isSelected());
    }

    private MultipartFile createProfileImage() {
        return new MockMultipartFile(
                "profileImage",
                "profile.png",
                "image/png",
                "image-content".getBytes()
        );
    }
}
