package sak.metricstool.service;

import sak.metricstool.dto.BranchDTO;
import sak.metricstool.dto.CommitDTO;
import sak.metricstool.dto.MergeRequestDTO;
import sak.metricstool.dto.PipelineDTO;
import sak.metricstool.entity.Participant;
import sak.metricstool.entity.Team;
import sak.metricstool.entity.TeamMetric;
import sak.metricstool.exception.ResourceNotFoundException;
import sak.metricstool.integration.GitLabClient;
import sak.metricstool.repository.TeamMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * チームメトリクスの収集・計算・管理を行うサービスクラス
 */
@Service
public class TeamMetricService {

    private final TeamMetricRepository teamMetricRepository;
    private final TeamService teamService;
    private final GitLabClient gitLabClient;

    /**
     * コンストラクタインジェクション
     * @param teamMetricRepository TeamMetricリポジトリ
     * @param teamService Teamサービス
     * @param gitLabClient GitLabClient
     */
    @Autowired
    public TeamMetricService(TeamMetricRepository teamMetricRepository, TeamService teamService, GitLabClient gitLabClient) {
        this.teamMetricRepository = teamMetricRepository;
        this.teamService = teamService;
        this.gitLabClient = gitLabClient;
    }

    /**
     * 全てのチームメトリクスを取得します。
     * @return チームメトリクスのリスト
     */
    public List<TeamMetric> getAllTeamMetrics() {
        return teamMetricRepository.findAll();
    }

    /**
     * 特定のイベントに属する全チームメトリクスを取得します。
     * @param eventId イベントの一意識別子
     * @return チームメトリクスのリスト
     */
    public List<TeamMetric> getTeamMetricsByEventId(String eventId) {
        return teamMetricRepository.findByTeam_Event_EventId(eventId);
    }

    /**
     * 特定のチームに属するメトリクスを取得します。
     * @param teamId チームの一意識別子
     * @return チームメトリクスのリスト
     */
    public List<TeamMetric> getTeamMetricsByTeamId(Long teamId) {
        return teamMetricRepository.findByTeam_TeamId(teamId);
    }

    /**
     * 特定のチームメトリクスを取得します。
     * @param teamMetricId チームメトリクスの一意識別子
     * @return TeamMetricエンティティ
     * @throws ResourceNotFoundException チームメトリクスが見つからない場合
     */
    public TeamMetric getTeamMetricById(Long teamMetricId) {
        return teamMetricRepository.findById(teamMetricId)
                .orElseThrow(() -> new ResourceNotFoundException("TeamMetric not found with id: " + teamMetricId));
    }

    /**
     * GitLabからチームメトリクスデータを取得し、保存します。
     * イベント開始日から本日までのデータを取得します。
     * @param teamId チームの一意識別子
     * @return 保存されたTeamMetricエンティティ
     */
    public TeamMetric fetchAndSaveMetricsForTeam(Long teamId) {
        // チームを取得
        Team team = teamService.getTeamById(teamId);
        Long projectId = team.getTeamId(); // 仮定: teamIdとGitLabプロジェクトIDが1対1対応

        // イベントの開始日を取得
        LocalDateTime eventStartDate = team.getEvent().getStartDate();

        // チームに所属する参加者を取得
        List<Participant> participants = teamService.getParticipantsByTeamId(teamId);

        // 各参加者のコミット数を取得
        List<Integer> commitCounts = participants.stream()
                .map(p -> {
                    List<CommitDTO> commits = gitLabClient.getUserCommits(projectId, p.getParticipantGitlabEmail(), eventStartDate);
                    return commits.size();
                })
                .collect(Collectors.toList());

        // チーム全体のコミット数
        int totalCommits = commitCounts.stream().mapToInt(Integer::intValue).sum();

        // コミット数の標準偏差を計算
        double stdDevCommits = calculateStandardDeviation(commitCounts);

        // マージリクエストを取得
        List<MergeRequestDTO> mergedMRs = gitLabClient.getProjectMergeRequests(projectId, eventStartDate);

        // デプロイ頻度（Mainブランチへのマージ回数）
        int deploymentFrequency = mergedMRs.size();

        // 変更失敗率（テスト失敗の割合）
        long failedDeployments = 0;
        for (MergeRequestDTO mr : mergedMRs) {
            // マージリクエストに関連するパイプラインを取得
            List<PipelineDTO> pipelines = gitLabClient.getPipelinesForMergeRequest(projectId, mr.getId());
            // 最新のパイプラインを取得
            PipelineDTO latestPipeline = pipelines.stream()
                    .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())) // IDが大きいほど新しい
                    .findFirst()
                    .orElse(null);
            if (latestPipeline != null && !"success".equalsIgnoreCase(latestPipeline.getStatus())) {
                failedDeployments++;
            }
        }

        double changeFailureRate = deploymentFrequency > 0 ? ((double) failedDeployments / deploymentFrequency) * 100 : 0.0;

        // ブランチ情報を取得
        List<BranchDTO> branches = gitLabClient.getProjectBranches(projectId);

        // 変更リードタイム（Storyブランチの平均生存時間）
        double changeLeadTime = calculateAverageLeadTime(branches, eventStartDate);

        // TeamMetricエンティティを作成
        TeamMetric teamMetric = TeamMetric.builder()
                .team(team)
                .commits(totalCommits)
                .stdDevCommits(stdDevCommits)
                .deploymentFrequency(deploymentFrequency)
                .changeFailureRate(changeFailureRate)
                .changeLeadTime(changeLeadTime)
                .metricDate(LocalDate.now())
                .build();

        // データベースに保存
        return teamMetricRepository.save(teamMetric);
    }

    /**
     * 全てのチームのメトリクスデータを取得し、保存します。
     * @return 保存されたTeamMetricエンティティのリスト
     */
    public List<TeamMetric> fetchAndSaveMetricsForAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return teams.stream()
                .map(team -> fetchAndSaveMetricsForTeam(team.getTeamId()))
                .collect(Collectors.toList());
    }

    /**
     * チームメトリクスを更新します。
     * @param teamMetricId チームメトリクスの一意識別子
     * @param teamMetricDetails 更新内容を含むTeamMetricエンティティ
     * @return 更新されたTeamMetricエンティティ
     */
    public TeamMetric updateTeamMetric(Long teamMetricId, TeamMetric teamMetricDetails) {
        TeamMetric teamMetric = getTeamMetricById(teamMetricId);
        teamMetric.setCommits(teamMetricDetails.getCommits());
        teamMetric.setStdDevCommits(teamMetricDetails.getStdDevCommits());
        teamMetric.setDeploymentFrequency(teamMetricDetails.getDeploymentFrequency());
        teamMetric.setChangeFailureRate(teamMetricDetails.getChangeFailureRate());
        teamMetric.setChangeLeadTime(teamMetricDetails.getChangeLeadTime());
        // metricDateは更新しない
        return teamMetricRepository.save(teamMetric);
    }

    /**
     * チームメトリクスを削除します。
     * @param teamMetricId チームメトリクスの一意識別子
     */
    public void deleteTeamMetric(Long teamMetricId) {
        TeamMetric teamMetric = getTeamMetricById(teamMetricId);
        teamMetricRepository.delete(teamMetric);
    }

    /**
     * コミット数の標準偏差を計算します。
     * @param numbers コミット数のリスト
     * @return 標準偏差
     */
    private double calculateStandardDeviation(List<Integer> numbers) {
        if (numbers.isEmpty()) return 0.0;
        double mean = numbers.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = numbers.stream()
                .mapToDouble(num -> Math.pow(num - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * 変更リードタイム（Storyブランチの平均生存時間）を計算します。
     * @param branches ブランチのリスト
     * @param since 開始日時（イベント開始日）
     * @return 平均リードタイム（時間単位）
     */
    private double calculateAverageLeadTime(List<BranchDTO> branches, LocalDateTime since) {
        List<Long> leadTimes = branches.stream()
                .filter(branch -> branch.getName().startsWith("story")) // Storyブランチのみ対象
                .filter(branch -> branch.getMergedAt() != null) // マージ済みブランチのみ対象
                .filter(branch -> branch.getCreatedAt().isAfter(since) || branch.getCreatedAt().isEqual(since)) // イベント開始日以降
                .map(branch -> java.time.Duration.between(branch.getCreatedAt(), branch.getMergedAt()).toHours())
                .collect(Collectors.toList());

        if (leadTimes.isEmpty()) return 0.0;

        OptionalDouble average = leadTimes.stream().mapToLong(Long::longValue).average();
        return average.isPresent() ? average.getAsDouble() : 0.0;
    }
}
