package com.kkirok.server.domain.meal.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.member.domain.Member;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "meal_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time_slot", nullable = false)
    private MealTimeSlot mealTimeSlot;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_category", nullable = false)
    private MealCategory category;

    @Column(name = "is_ai_analyzed", nullable = false)
    private boolean aiAnalyzed;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false)
    private ScanType scanType;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @OneToMany(mappedBy = "mealRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealAiAnalysis> mealAiAnalyses = new ArrayList<>();

    @Builder
    private MealRecord(Member member, LocalDateTime recordedAt, LocalDate mealDate,
                       MealTimeSlot mealTimeSlot, String name, MealCategory category,
                       boolean aiAnalyzed, ScanType scanType, Integer healthScore, String memo) {
        this.member = member;
        this.recordedAt = recordedAt;
        this.mealDate = mealDate;
        this.mealTimeSlot = mealTimeSlot;
        this.name = name;
        this.category = category;
        this.aiAnalyzed = aiAnalyzed;
        this.scanType = scanType;
        this.healthScore = healthScore;
        this.memo = memo;
    }

    /** 직접 입력으로 식단 기록 생성 */
    public static MealRecord createManual(Member member, MealCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return MealRecord.builder()
                .member(member)
                .recordedAt(now)
                .mealDate(now.toLocalDate())
                .mealTimeSlot(request.mealTimeSlot())
                .name(request.foodName())
                .category(request.category())
                .aiAnalyzed(false)
                .scanType(ScanType.DIRECT)
                .memo(request.memo())
                .build();
    }

    /** 카메라/앨범 AI 분석으로 식단 기록 생성 */
    public static MealRecord createByAi(Member member, ScanType scanType) {
        LocalDateTime now = LocalDateTime.now();
        return MealRecord.builder()
                .member(member)
                .recordedAt(now)
                .mealDate(now.toLocalDate())
                .mealTimeSlot(MealTimeSlot.BREAKFAST) // AI 분석 후 업데이트 가능
                .name("")                             // AI 분석 후 업데이트
                .category(MealCategory.MEAL)
                .aiAnalyzed(true)
                .scanType(scanType)
                .build();
    }

    /** 식단 수정 */
    public void update(MealUpdateRequest request) {
        this.mealTimeSlot = request.mealTimeSlot();
        this.category = request.category();
        this.name = request.foodName();
        this.memo = request.memo();
    }

    /** AI 분석 결과 반영 후 이름/시간대 업데이트 */
    public void applyAiResult(String detectedName, MealTimeSlot timeSlot) {
        this.name = detectedName;
        this.mealTimeSlot = timeSlot;
    }
}
