package sak.metricstool.controller;

import sak.metricstool.entity.TeamMetric;
import sak.metricstool.service.TeamMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * チームメトリクスに関するAPIエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/api/team-metrics")
public class TeamMetricController {

    private final TeamMetricService teamMetricService;

    /**
     * コンストラクタインジェクション
     * @param teamMetricService TeamMetricサービス
     */
    @Autowired
    public TeamMetricController(TeamMetricService teamMetricService) {
        this.teamMetricService = teamMetricService;
    }

    /**
     * 全てのチームメトリクスを取得します。
     * @return チームメトリクスのリスト
     */
    @GetMapping
    public ResponseEntity<List<TeamMetric>> getAllTeamMetrics() {
        List<TeamMetric> metrics = teamMetricService.getAllTeamMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * GitLabから全チームのメトリクスデータを取得し、保存します。
     * @return 保存されたTeamMetricエンティティのリスト
     */
    @PostMapping("/fetch-all")
    public ResponseEntity<List<TeamMetric>> fetchAndSaveMetricsForAllTeams() {
        List<TeamMetric> metrics = teamMetricService.fetchAndSaveMetricsForAllTeams();
        return ResponseEntity.status(201).body(metrics);
    }

    /**
     * 特定のイベントに属する全チームメトリクスを取得します。
     * @param eventId イベントの一意識別子
     * @return チームメトリクスのリスト
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TeamMetric>> getTeamMetricsByEventId(@PathVariable String eventId) {
        List<TeamMetric> metrics = teamMetricService.getTeamMetricsByEventId(eventId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 特定のチームに属するメトリクスを取得します。
     * @param teamId チームの一意識別子
     * @return チームメトリクスのリスト
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamMetric>> getTeamMetricsByTeamId(@PathVariable Long teamId) {
        List<TeamMetric> metrics = teamMetricService.getTeamMetricsByTeamId(teamId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 特定のチームメトリクスを取得します。
     * @param teamMetricId チームメトリクスの一意識別子
     * @return TeamMetricエンティティ
     */
    @GetMapping("/{teamMetricId}")
    public ResponseEntity<TeamMetric> getTeamMetricById(@PathVariable Long teamMetricId) {
        TeamMetric metric = teamMetricService.getTeamMetricById(teamMetricId);
        return ResponseEntity.ok(metric);
    }

    /**
     * GitLabから特定のチームのメトリクスデータを取得し、保存します。
     * @param teamId チームの一意識別子
     * @return 保存されたTeamMetricエンティティ
     */
    @PostMapping("/team/{teamId}/fetch")
    public ResponseEntity<TeamMetric> fetchAndSaveMetricsForTeam(@PathVariable Long teamId) {
        TeamMetric metric = teamMetricService.fetchAndSaveMetricsForTeam(teamId);
        return ResponseEntity.status(201).body(metric);
    }

    /**
     * 任意で新しいチームメトリクスを作成します。
     * @param teamId チームの一意識別子
     * @return 作成されたTeamMetricエンティティ
     */
    @PostMapping("/team/{teamId}")
    public ResponseEntity<TeamMetric> createTeamMetric(@PathVariable Long teamId) {
        TeamMetric createdMetric = teamMetricService.fetchAndSaveMetricsForTeam(teamId);
        return ResponseEntity.status(201).body(createdMetric);
    }

    /**
     * 既存のチームメトリクスを更新します。
     * @param teamMetricId チームメトリクスの一意識別子
     * @param teamMetricDetails 更新内容を含むTeamMetricエンティティ
     * @return 更新されたTeamMetricエンティティ
     */
    @PutMapping("/{teamMetricId}")
    public ResponseEntity<TeamMetric> updateTeamMetric(@PathVariable Long teamMetricId, @RequestBody TeamMetric teamMetricDetails) {
        TeamMetric updatedMetric = teamMetricService.updateTeamMetric(teamMetricId, teamMetricDetails);
        return ResponseEntity.ok(updatedMetric);
    }

    /**
     * チームメトリクスを削除します。
     * @param teamMetricId チームメトリクスの一意識別子
     * @return 空のレスポンス
     */
    @DeleteMapping("/{teamMetricId}")
    public ResponseEntity<Void> deleteTeamMetric(@PathVariable Long teamMetricId) {
        teamMetricService.deleteTeamMetric(teamMetricId);
        return ResponseEntity.noContent().build();
    }
}
