package sak.metricstool.service;

import sak.metricstool.dto.BranchDTO;
import sak.metricstool.dto.CommitDTO;
import sak.metricstool.dto.MergeRequestDTO;
import sak.metricstool.dto.PipelineDTO;
import sak.metricstool.entity.Participant;
import sak.metricstool.entity.ParticipantMetric;
import sak.metricstool.exception.ResourceNotFoundException;
import sak.metricstool.integration.GitLabClient;
import sak.metricstool.repository.ParticipantMetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * 参加者メトリクスの収集・計算・管理を行うサービスクラス
 */
@Service
public class ParticipantMetricService {

    private final ParticipantMetricRepository participantMetricRepository;
    private final ParticipantService participantService;
    private final GitLabClient gitLabClient;
    private final TeamService teamService;

    /**
     * コンストラクタインジェクション
     * @param participantMetricRepository ParticipantMetricリポジトリ
     * @param participantService Participantサービス
     * @param gitLabClient GitLabClient
     * @param teamService Teamサービス
     */
    @Autowired
    public ParticipantMetricService(ParticipantMetricRepository participantMetricRepository, ParticipantService participantService, GitLabClient gitLabClient, TeamService teamService) {
        this.participantMetricRepository = participantMetricRepository;
        this.participantService = participantService;
        this.gitLabClient = gitLabClient;
        this.teamService = teamService;
    }

        /**
     * 特定のチームに属する全参加者メトリクスを取得します。
     * @param teamId チームの一意識別子
     * @return 参加者メトリクスのリスト
     */
    public List<ParticipantMetric> getParticipantMetricsByTeamId(Long teamId) {
        // チームに所属する参加者を取得
        List<Participant> participants = teamService.getParticipantsByTeamId(teamId);
        List<Long> participantIds = participants.stream()
                .map(Participant::getParticipantId)
                .collect(Collectors.toList());
        // 参加者メトリクスを取得
        return participantMetricRepository.findByParticipant_ParticipantIdIn(participantIds);
    }

    /**
     * GitLabから個人メトリクスデータを取得し、保存します。
     * イベント開始日から本日までのデータを取得します。
     * @param participantId 参加者の一意識別子
     * @return 保存されたParticipantMetricエンティティ
     */
    public ParticipantMetric fetchAndSaveMetricsForParticipant(Long participantId) {
        // 参加者を取得
        Participant participant = participantService.getParticipantById(participantId);
        Long projectId = participant.getTeam().getTeamId(); // 仮定: teamIdとGitLabプロジェクトIDが1対1対応
        String email = participant.getParticipantGitlabEmail();

        // イベントの開始日を取得
        LocalDateTime eventStartDate = participant.getTeam().getEvent().getStartDate();

        // コミット数を取得
        List<CommitDTO> commits = gitLabClient.getUserCommits(projectId, email, eventStartDate);
        int commitCount = commits.size();

        // マージリクエストを取得
        List<MergeRequestDTO> allMRs = gitLabClient.getProjectMergeRequests(projectId, eventStartDate);

        // 個人が作成したMainブランチへのマージ済みマージリクエストをフィルタリング
        List<MergeRequestDTO> userMainMRs = allMRs.stream()
                .filter(mr -> "main".equalsIgnoreCase(mr.getTargetBranch()))
                .filter(mr -> email.equalsIgnoreCase(mr.getAuthorEmail()))
                .collect(Collectors.toList());

        // デプロイ頻度（Mainブランチへのマージ回数）
        int deploymentFrequency = userMainMRs.size();

        // 変更失敗率（テスト失敗の割合）
        long failedDeployments = 0;
        for (MergeRequestDTO mr : userMainMRs) {
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

        // ParticipantMetricエンティティを作成
        ParticipantMetric participantMetric = ParticipantMetric.builder()
                .participant(participant)
                .commits(commitCount)
                .deploymentFrequency(deploymentFrequency)
                .changeFailureRate(changeFailureRate)
                .changeLeadTime(changeLeadTime)
                .metricDate(LocalDate.now())
                .build();

        // データベースに保存
        return participantMetricRepository.save(participantMetric);
    }

    /**
     * 全参加者のメトリクスデータをGitLabから取得し、保存します。
     * @return 保存されたParticipantMetricエンティティのリスト
     */
    public List<ParticipantMetric> fetchAndSaveMetricsForAllParticipants() {
        List<Participant> participants = participantService.getAllParticipants();
        return participants.stream()
                .map(p -> fetchAndSaveMetricsForParticipant(p.getParticipantId()))
                .collect(Collectors.toList());
    }

    /**
     * 全ての参加者メトリクスを取得します。
     * @return ParticipantMetricのリスト
     */
    public List<ParticipantMetric> getAllParticipantMetrics() {
        return participantMetricRepository.findAll();
    }

    /**
     * 特定の参加者に属するメトリクスを取得します。
     * @param participantId 参加者の一意識別子
     * @return ParticipantMetricのリスト
     */
    public List<ParticipantMetric> getParticipantMetricsByParticipantId(Long participantId) {
        return participantMetricRepository.findByParticipant_ParticipantId(participantId);
    }

    /**
     * 特定の参加者メトリクスを取得します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     * @return ParticipantMetricエンティティ
     * @throws ResourceNotFoundException 参加者メトリクスが見つからない場合
     */
    public ParticipantMetric getParticipantMetricById(Long participantMetricId) {
        return participantMetricRepository.findById(participantMetricId)
                .orElseThrow(() -> new ResourceNotFoundException("ParticipantMetric not found with id: " + participantMetricId));
    }

    /**
     * 参加者メトリクスを更新します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     * @param participantMetricDetails 更新内容を含むParticipantMetricエンティティ
     * @return 更新されたParticipantMetricエンティティ
     */
    public ParticipantMetric updateParticipantMetric(Long participantMetricId, ParticipantMetric participantMetricDetails) {
        ParticipantMetric participantMetric = getParticipantMetricById(participantMetricId);
        participantMetric.setCommits(participantMetricDetails.getCommits());
        participantMetric.setDeploymentFrequency(participantMetricDetails.getDeploymentFrequency());
        participantMetric.setChangeFailureRate(participantMetricDetails.getChangeFailureRate());
        participantMetric.setChangeLeadTime(participantMetricDetails.getChangeLeadTime());
        // metricDateは更新しない
        return participantMetricRepository.save(participantMetric);
    }

    /**
     * 参加者メトリクスを削除します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     */
    public void deleteParticipantMetric(Long participantMetricId) {
        ParticipantMetric participantMetric = getParticipantMetricById(participantMetricId);
        participantMetricRepository.delete(participantMetric);
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
