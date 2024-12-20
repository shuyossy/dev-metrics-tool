package sak.metricstool.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * GitLabのコミット情報を保持するDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitDTO {
    private String authorEmail; // コミット作者のEメール
    private String authorName; // コミット作者の名前
    private LocalDateTime committedDate; // コミット日時
}
