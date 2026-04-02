package com.kkirok.server.domain.meal.api;

import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthResponse;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/calendar")
@RoleUserAuth
public class CalendarController implements CalendarApi{

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<CalendarMonthResponse>> calendarInfo(@RequestParam Integer year, @RequestParam Integer month, @RequestParam Integer startDayOfWeek) {
        return null;
    }

}
