package sak.metricstool.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * GitLabのマージリクエスト情報を保持するDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergeRequestDTO {
    private Long id; // マージリクエストの一意識別子
    private String authorEmail; // マージリクエスト作者のEメール
    private String targetBranch; // マージ先ブランチ（通常はmain）
    private LocalDateTime mergedAt; // マージ日時
}
