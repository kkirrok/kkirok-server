package com.kkirok.server.domain.report.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "weekly_report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WeeklyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate; // 해당 주의 시작 날짜. 월요일이어야 함.

    @Builder.Default
    @Column(name = "total_record_days", nullable = false)
    private Integer totalRecordDays = 0;

    @Column(name = "kcal_mon")
    @Builder.Default
    private Integer kcalMon = 0;

    @Column(name = "kcal_tue")
    @Builder.Default
    private Integer kcalTue = 0;

    @Column(name = "kcal_wed")
    @Builder.Default
    private Integer kcalWed = 0;

    @Column(name = "kcal_thu")
    @Builder.Default
    private Integer kcalThu = 0;

    @Column(name = "kcal_fri")
    @Builder.Default
    private Integer kcalFri = 0;

    @Column(name = "kcal_sat")
    @Builder.Default
    private Integer kcalSat = 0;

    @Column(name = "kcal_sun")
    @Builder.Default
    private Integer kcalSun = 0;

    @Column(name = "avg_proein", precision = 8, scale = 2)
    @Builder.Default
    private Long avgProtein = 0L;

    @Column(name = "avg_carbohydrate", precision = 8, scale = 2)
    @Builder.Default
    private Long avgCarbohydrate = 0L;

    @Column(name = "avg_sugar", precision = 8, scale = 2)
    @Builder.Default
    private Long avgSugar = 0L;

    @Column(name = "avg_fat", precision = 8, scale = 2)
    @Builder.Default
    private Long avgFat = 0L;

    @Column(name = "avg_sodium", precision = 8, scale = 2)
    @Builder.Default
    private Long avgSodium = 0L;

    @Column(name = "intake_comment", length = 255)
    private String intakeComment; // 섭취 코맨트

    @Column(name = "main_kcal_title", length = 100)
    private String mainKcalTitle; // 칼로리 코맨트 제목

    @Column(name = "main_kcal_comment", length = 255)
    private String mainKcalComment; // 칼로리 코맨트 내용



}
