package sak.metricstool.integration;

import sak.metricstool.dto.BranchDTO;
import sak.metricstool.dto.CommitDTO;
import sak.metricstool.dto.MergeRequestDTO;
import sak.metricstool.dto.PipelineDTO;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * テスト用のGitLabClient実装クラス
 */
public class MockGitLabClient implements GitLabClient {

    /**
     * 指定されたプロジェクトとユーザーに関連するコミットを取得します（モックデータ）。
     */
    @Override
    public List<CommitDTO> getUserCommits(Long projectId, String authorEmail, LocalDateTime since) {
        // モックデータを返す
        return Arrays.asList(
            CommitDTO.builder()
                .authorEmail(authorEmail)
                .authorName("Test User")
                .committedDate(LocalDateTime.now().minusDays(1))
                .build(),
            CommitDTO.builder()
                .authorEmail(authorEmail)
                .authorName("Test User")
                .committedDate(LocalDateTime.now().minusDays(2))
                .build()
        );
    }

    /**
     * 指定されたプロジェクトのマージリクエストを取得します（モックデータ）。
     */
    @Override
    public List<MergeRequestDTO> getProjectMergeRequests(Long projectId, LocalDateTime since) {
        // モックデータを返す
        return Arrays.asList(
            MergeRequestDTO.builder()
                .id(101L)
                .authorEmail("test.user@example.com")
                .targetBranch("main")
                .mergedAt(LocalDateTime.now().minusDays(1))
                .build(),
            MergeRequestDTO.builder()
                .id(102L)
                .authorEmail("test.user@example.com")
                .targetBranch("main")
                .mergedAt(LocalDateTime.now().minusDays(3))
                .build()
        );
    }

    /**
     * 指定されたプロジェクトのブランチを取得します（モックデータ）。
     */
    @Override
    public List<BranchDTO> getProjectBranches(Long projectId) {
        // モックデータを返す
        return Arrays.asList(
            BranchDTO.builder()
                .name("main")
                .createdAt(LocalDateTime.now().minusMonths(1))
                .mergedAt(LocalDateTime.now().minusDays(1))
                .build(),
            BranchDTO.builder()
                .name("story/feature1")
                .createdAt(LocalDateTime.now().minusDays(5))
                .mergedAt(LocalDateTime.now().minusDays(2))
                .build()
        );
    }

    /**
     * 指定されたプロジェクトとマージリクエストに関連するパイプラインを取得します（モックデータ）。
     */
    @Override
    public List<PipelineDTO> getPipelinesForMergeRequest(Long projectId, Long mergeRequestId) {
        // モックデータを返す
        if (mergeRequestId == 101L) {
            return Arrays.asList(
                PipelineDTO.builder()
                    .id(1001L)
                    .status("success")
                    .ref("main")
                    .sha("abcdef123456")
                    .build()
            );
        } else if (mergeRequestId == 102L) {
            return Arrays.asList(
                PipelineDTO.builder()
                    .id(1002L)
                    .status("failed")
                    .ref("main")
                    .sha("abcdef123457")
                    .build()
            );
        } else {
            return Collections.emptyList();
        }
    }
}
