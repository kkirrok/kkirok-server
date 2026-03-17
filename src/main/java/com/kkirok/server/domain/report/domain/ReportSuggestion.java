package com.kkirok.server.domain.report.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "report_suggestions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportSuggestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private WeeklyReport report;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255, nullable = false)
    private String content;

    @Builder
    private ReportSuggestion(WeeklyReport report, String title, String content) {
        this.report = report;
        this.title = title;
        this.content = content;
    }

    public static ReportSuggestion create(WeeklyReport report) { // TODO: 요청 DTO 넣어서 하기
        return ReportSuggestion.builder()
                .report(report)
//                .title(title)
//                .content(content)
                .build();
    }

}
