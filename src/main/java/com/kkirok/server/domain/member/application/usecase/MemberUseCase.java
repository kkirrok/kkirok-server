package com.kkirok.server.domain.member.application.usecase;

import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;

public interface MemberUseCase {
    Member findMemberByMemberId(Long memberId);

    boolean checkMemberExistsBySocialIdAndSocialType(Long socialId, SocialType socialType);

    Member findMemberBySocialIdAndSocialType(Long socialId, SocialType socialType);

    void deleteUser(Long id);

    long countMembers();

    void updateMember(Member member);

}
