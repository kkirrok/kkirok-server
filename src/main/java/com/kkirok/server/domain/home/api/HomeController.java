package com.kkirok.server.domain.home.api;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.domain.home.application.service.HomeService;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/home")
@RoleUserAuth
public class HomeController implements HomeApi {

    private final HomeService homeService;

    @Override
    @GetMapping
    public ResponseEntity<HomeResponse> getHome(@CurrentMember Long memberId) {
        return ResponseEntity.ok(homeService.getHomeInfo(memberId));
    }

}
