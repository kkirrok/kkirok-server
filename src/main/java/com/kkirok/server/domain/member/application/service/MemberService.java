package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.dao.UserRepository;
import com.kkirok.server.domain.user.domain.Users;
import com.kkirok.server.global.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService implements MemberUseCase {
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkMemberExistsBySocialIdAndSocialType(final Long socialId, final SocialType socialType) {
        return memberRepository.findBySocialTypeAndSocialId(socialId, socialType).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Member findMemberBySocialIdAndSocialType(final Long socialId, final SocialType socialType) {
        return memberRepository.findBySocialTypeAndSocialId(socialId, socialType)
                .orElseThrow(() -> new NotFoundException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteUser(final Long id) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(MemberErrorCode.MEMBER_NOT_FOUND));

        userRepository.delete(users);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMembers() {
        return memberRepository.count();
    }

    @Override
    public void updateMember(Member member) {
        memberRepository.save(member);
    }
}
