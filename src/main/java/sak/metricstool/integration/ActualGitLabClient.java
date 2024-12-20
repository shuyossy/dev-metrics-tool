package sak.metricstool.integration;

import sak.metricstool.dto.BranchDTO;
import sak.metricstool.dto.CommitDTO;
import sak.metricstool.dto.MergeRequestDTO;
import sak.metricstool.dto.PipelineDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GitLab APIとの連携を実装するクラス
 */
public class ActualGitLabClient implements GitLabClient {

    @Value("${gitlab.api.base-url}")
    private String baseUrl; // GitLab APIのベースURL

    @Value("${gitlab.api.token}")
    private String token; // GitLab APIアクセストークン

    private final RestTemplate restTemplate;

    /**
     * コンストラクタインジェクション
     * @param restTemplate RestTemplateのインスタンス
     */
    public ActualGitLabClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * GitLab APIに必要なヘッダーを作成します。
     * @return HttpHeadersオブジェクト
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token); // ベアラートークンを設定
        headers.setContentType(MediaType.APPLICATION_JSON); // コンテンツタイプをJSONに設定
        return headers;
    }

    /**
     * 指定されたプロジェクトとユーザーに関連するコミットを取得します。
     */
    @Override
    public List<CommitDTO> getUserCommits(Long projectId, String authorEmail, LocalDateTime since) {
        // GitLab APIのコミット取得エンドポイント
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String sinceParam = since.format(formatter);
        String url = String.format("%s/projects/%d/repository/commits?author_email=%s&since=%s", 
                                   baseUrl, projectId, authorEmail, sinceParam);
        
        // HTTPリクエストを作成
        ResponseEntity<CommitDTO[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                CommitDTO[].class
        );
        
        // レスポンスボディを取得
        CommitDTO[] commits = response.getBody();
        
        // nullチェックとリスト変換
        return commits != null ? Arrays.asList(commits) : Collections.emptyList();
    }

    /**
     * 指定されたプロジェクトのマージリクエストを取得します。
     */
    @Override
    public List<MergeRequestDTO> getProjectMergeRequests(Long projectId, LocalDateTime since) {
        // GitLab APIのマージリクエスト取得エンドポイント（マージ済みのみ）
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String sinceParam = since.format(formatter);
        String url = String.format("%s/projects/%d/merge_requests?state=merged&target_branch=main&updated_after=%s", 
                                   baseUrl, projectId, sinceParam);
        
        // HTTPリクエストを作成
        ResponseEntity<MergeRequestDTO[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                MergeRequestDTO[].class
        );
        
        // レスポンスボディを取得
        MergeRequestDTO[] mrs = response.getBody();
        
        // nullチェックとリスト変換
        return mrs != null ? Arrays.asList(mrs) : Collections.emptyList();
    }

    /**
     * 指定されたプロジェクトのブランチを取得します。
     */
    @Override
    public List<BranchDTO> getProjectBranches(Long projectId) {
        // GitLab APIのブランチ取得エンドポイント
        String url = String.format("%s/projects/%d/repository/branches", baseUrl, projectId);
        
        // HTTPリクエストを作成
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                BranchDTO[].class
        );
        
        // レスポンスボディを取得
        BranchDTO[] branches = response.getBody();
        
        // nullチェックとリスト変換
        return branches != null ? Arrays.asList(branches) : Collections.emptyList();
    }

    /**
     * 指定されたプロジェクトとマージリクエストに関連するパイプラインを取得します。
     */
    @Override
    public List<PipelineDTO> getPipelinesForMergeRequest(Long projectId, Long mergeRequestId) {
        // GitLab APIのマージリクエストに関連するパイプライン取得エンドポイント
        String url = String.format("%s/projects/%d/merge_requests/%d/pipelines", baseUrl, projectId, mergeRequestId);
        
        // HTTPリクエストを作成
        ResponseEntity<PipelineDTO[]> response = restTemplate.exchange(
                url, 
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                PipelineDTO[].class
        );
        
        // レスポンスボディを取得
        PipelineDTO[] pipelines = response.getBody();
        
        // nullチェックとリスト変換
        return pipelines != null ? Arrays.asList(pipelines) : Collections.emptyList();
    }
}
