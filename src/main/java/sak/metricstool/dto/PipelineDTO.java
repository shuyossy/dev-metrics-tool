package sak.metricstool.dto;

import lombok.*;

/**
 * GitLabのパイプライン情報を保持するDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineDTO {
    private Long id; // パイプラインの一意識別子
    private String status; // パイプラインの状態（e.g., success, failed, running）
    private String ref; // パイプラインが実行されたブランチ名
    private String sha; // コミットSHA
}
