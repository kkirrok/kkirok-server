package com.kkirok.server.support.integration;

import com.kkirok.server.domain.member.dao.AuthIdentityRepository;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.user.dao.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected AuthIdentityRepository authIdentityRepository;

    @Autowired
    protected UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        authIdentityRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
