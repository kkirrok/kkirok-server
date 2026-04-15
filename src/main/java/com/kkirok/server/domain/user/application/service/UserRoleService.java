package com.kkirok.server.domain.user.application.service;

import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.domain.user.domain.Users;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {

    public void promoteToUser(final Users user) {
        if (user.getRole() == Role.PENDING) {
            user.changeRole(Role.USER);
        }
    }
}
