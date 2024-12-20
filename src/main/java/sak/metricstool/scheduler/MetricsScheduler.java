package sak.metricstool.scheduler;

import sak.metricstool.service.TeamMetricService;
import sak.metricstool.service.ParticipantMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * メトリクス収集を定期的に実行するスケジューラー
 */
@Component
public class MetricsScheduler {

    private final TeamMetricService teamMetricService;
    private final ParticipantMetricService participantMetricService;

    /**
     * コンストラクタインジェクション
     * @param teamMetricService TeamMetricサービス
     * @param participantMetricService ParticipantMetricサービス
     */
    @Autowired
    public MetricsScheduler(TeamMetricService teamMetricService, ParticipantMetricService participantMetricService) {
        this.teamMetricService = teamMetricService;
        this.participantMetricService = participantMetricService;
    }

    /**
     * 毎日午前1時にメトリクス収集を実行します。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void collectMetricsDaily() {
        // 全チームのメトリクスを収集・保存
        teamMetricService.fetchAndSaveMetricsForAllTeams();
        // 全参加者のメトリクスを収集・保存
        participantMetricService.fetchAndSaveMetricsForAllParticipants();
    }
}
