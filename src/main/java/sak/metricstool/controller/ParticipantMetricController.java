package sak.metricstool.controller;

import sak.metricstool.entity.ParticipantMetric;
import sak.metricstool.service.ParticipantMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 個人メトリクスに関するAPIエンドポイントを提供するコントローラー
 */
@RestController
@RequestMapping("/api/participant-metrics")
public class ParticipantMetricController {

    private final ParticipantMetricService participantMetricService;

    /**
     * コンストラクタインジェクション
     * @param participantMetricService ParticipantMetricサービス
     */
    @Autowired
    public ParticipantMetricController(ParticipantMetricService participantMetricService) {
        this.participantMetricService = participantMetricService;
    }

    /**
     * 全ての参加者メトリクスを取得します。
     * @return 参加者メトリクスのリスト
     */
    @GetMapping
    public ResponseEntity<List<ParticipantMetric>> getAllParticipantMetrics() {
        List<ParticipantMetric> metrics = participantMetricService.getAllParticipantMetrics();
        return ResponseEntity.ok(metrics);
    }


    /**
     * 特定のチームに属する参加者メトリクスを取得します。
     * @param teamId チームの一意識別子
     * @return 参加者メトリクスのリスト
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ParticipantMetric>> getParticipantMetricsByTeamId(@PathVariable Long teamId) {
        List<ParticipantMetric> metrics = participantMetricService.getParticipantMetricsByTeamId(teamId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 特定の参加者に属するメトリクスを取得します。
     * @param participantId 参加者の一意識別子
     * @return 参加者メトリクスのリスト
     */
    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<ParticipantMetric>> getParticipantMetricsByParticipantId(@PathVariable Long participantId) {
        List<ParticipantMetric> metrics = participantMetricService.getParticipantMetricsByParticipantId(participantId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * 特定の参加者メトリクスを取得します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     * @return ParticipantMetricエンティティ
     */
    @GetMapping("/{participantMetricId}")
    public ResponseEntity<ParticipantMetric> getParticipantMetricById(@PathVariable Long participantMetricId) {
        ParticipantMetric metric = participantMetricService.getParticipantMetricById(participantMetricId);
        return ResponseEntity.ok(metric);
    }

    /**
     * GitLabから特定の参加者のメトリクスデータを取得し、保存します。
     * @param participantId 参加者の一意識別子
     * @return 保存されたParticipantMetricエンティティ
     */
    @PostMapping("/participant/{participantId}/fetch")
    public ResponseEntity<ParticipantMetric> fetchAndSaveMetricsForParticipant(@PathVariable Long participantId) {
        ParticipantMetric metric = participantMetricService.fetchAndSaveMetricsForParticipant(participantId);
        return ResponseEntity.status(201).body(metric);
    }

    /**
     * GitLabから全参加者のメトリクスデータを取得し、保存します。
     * @return 保存されたParticipantMetricエンティティのリスト
     */
    @PostMapping("/fetch-all")
    public ResponseEntity<List<ParticipantMetric>> fetchAndSaveMetricsForAllParticipants() {
        List<ParticipantMetric> metrics = participantMetricService.fetchAndSaveMetricsForAllParticipants();
        return ResponseEntity.status(201).body(metrics);
    }

    /**
     * 任意で新しい参加者メトリクスを作成します。
     * @param participantId 参加者の一意識別子
     * @return 作成されたParticipantMetricエンティティ
     */
    @PostMapping("/participant/{participantId}")
    public ResponseEntity<ParticipantMetric> createParticipantMetric(@PathVariable Long participantId) {
        ParticipantMetric createdMetric = participantMetricService.fetchAndSaveMetricsForParticipant(participantId);
        return ResponseEntity.status(201).body(createdMetric);
    }

    /**
     * 既存の参加者メトリクスを更新します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     * @param participantMetricDetails 更新内容を含むParticipantMetricエンティティ
     * @return 更新されたParticipantMetricエンティティ
     */
    @PutMapping("/{participantMetricId}")
    public ResponseEntity<ParticipantMetric> updateParticipantMetric(@PathVariable Long participantMetricId, @RequestBody ParticipantMetric participantMetricDetails) {
        ParticipantMetric updatedMetric = participantMetricService.updateParticipantMetric(participantMetricId, participantMetricDetails);
        return ResponseEntity.ok(updatedMetric);
    }

    /**
     * 参加者メトリクスを削除します。
     * @param participantMetricId 参加者メトリクスの一意識別子
     * @return 空のレスポンス
     */
    @DeleteMapping("/{participantMetricId}")
    public ResponseEntity<Void> deleteParticipantMetric(@PathVariable Long participantMetricId) {
        participantMetricService.deleteParticipantMetric(participantMetricId);
        return ResponseEntity.noContent().build();
    }
}
