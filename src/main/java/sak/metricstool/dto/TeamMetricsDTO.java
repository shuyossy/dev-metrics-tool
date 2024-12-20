package sak.metricstool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * チームメトリクスデータを保持するDTOクラス。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMetricsDTO {
    private Integer commits; // チーム全体のコミット数
    private Double stdDevCommits; // チーム内個人別コミット数の標準偏差
    private Integer deploymentFrequency; // デプロイ頻度（Mainブランチへのマージ回数）
    private Double changeFailureRate; // 変更失敗率（Mainブランチマージ時のテスト失敗率）
    private Double changeLeadTime; // 変更リードタイム（Storyブランチの平均生存時間）
}
