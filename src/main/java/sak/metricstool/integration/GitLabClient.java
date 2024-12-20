package sak.metricstool.integration;

import sak.metricstool.dto.BranchDTO;
import sak.metricstool.dto.CommitDTO;
import sak.metricstool.dto.MergeRequestDTO;
import sak.metricstool.dto.PipelineDTO;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GitLab APIとの連携を抽象化するインターフェース
 */
public interface GitLabClient {
    /**
     * 指定されたプロジェクトとユーザーに関連するコミットを取得します。
     * @param projectId GitLabプロジェクトID
     * @param authorEmail コミット作者のEメール
     * @param since 開始日時（イベント開始日）
     * @return コミットのリスト
     */
    List<CommitDTO> getUserCommits(Long projectId, String authorEmail, LocalDateTime since);
    
    /**
     * 指定されたプロジェクトのマージリクエストを取得します。
     * @param projectId GitLabプロジェクトID
     * @param since 開始日時（イベント開始日）
     * @return マージリクエストのリスト
     */
    List<MergeRequestDTO> getProjectMergeRequests(Long projectId, LocalDateTime since);
    
    /**
     * 指定されたプロジェクトのブランチを取得します。
     * @param projectId GitLabプロジェクトID
     * @return ブランチのリスト
     */
    List<BranchDTO> getProjectBranches(Long projectId);
    
    /**
     * 指定されたプロジェクトとマージリクエストに関連するパイプラインを取得します。
     * @param projectId GitLabプロジェクトID
     * @param mergeRequestId マージリクエストID
     * @return パイプラインのリスト
     */
    List<PipelineDTO> getPipelinesForMergeRequest(Long projectId, Long mergeRequestId);
}
