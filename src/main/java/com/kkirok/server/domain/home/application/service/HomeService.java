package com.kkirok.server.domain.home.application.service;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.domain.notification.application.service.MealReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈화면에 필요한 도메인 정보를 가져와 조합하여 반환합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeService {

    private final MealReminderService mealReminderService;

    public HomeResponse getHomeInfo(Long memberId) {

        // 데이터 조회
        String string = mealReminderService.toString();

        // 조립 후 반환
        return new HomeResponse(
                null,
                null,
                null,
                null
        );
    }
}
