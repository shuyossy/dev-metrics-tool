package sak.metricstool.dto;

import lombok.*;

/**
 * サービス層で計算された個人メトリクスを保持するDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMetricsDTO {
    private String gitlabEmail; // GitLabユーザーのEメール
    private Integer commits; // コミット数
    private Integer deploymentFrequency; // デプロイ頻度
    private Double changeFailureRate; // 変更失敗率
    private Double changeLeadTime; // 変更リードタイム
}
