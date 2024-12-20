package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * チームメトリクスエンティティ
 */
@Entity
@Table(name = "team_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_metric_id")
    private Long teamMetricId; // メトリクスの一意識別子

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties("teamMetrics")
    private Team team; // メトリクスが属するチーム

    @Column(name = "commits", nullable = false)
    private Integer commits; // コミット数

    @Column(name = "std_dev_commits", nullable = false)
    private Double stdDevCommits; // コミット数の標準偏差

    @Column(name = "deployment_frequency", nullable = false)
    private Integer deploymentFrequency; // デプロイ頻度（Mainブランチへのマージ回数）

    @Column(name = "change_failure_rate", nullable = false)
    private Double changeFailureRate; // 変更失敗率（テスト失敗率）

    @Column(name = "change_lead_time", nullable = false)
    private Double changeLeadTime; // 変更リードタイム（Storyブランチの平均生存時間）

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate; // メトリクス収集日

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // レコード作成日時

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // レコード更新日時

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
