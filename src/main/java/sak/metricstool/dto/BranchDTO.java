package sak.metricstool.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * GitLabのブランチ情報を保持するDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO {
    private String name; // ブランチ名
    private LocalDateTime createdAt; // ブランチ作成日時
    private LocalDateTime mergedAt; // ブランチマージ日時（未マージの場合はnull）
}
