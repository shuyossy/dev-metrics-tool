package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sak.metricstool.entity.TeamMetric;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TeamMetricRepository extends JpaRepository<TeamMetric, Long> {
    /**
     * 特定のチームに属するメトリクスを取得するメソッド。
     * @param teamId チームの一意識別子
     * @return メトリクスのリスト
     */
    List<TeamMetric> findByTeam_TeamId(Long teamId);

    /**
     * 特定の日付範囲内のチームメトリクスを取得するメソッド。
     * @param startDate 開始日
     * @param endDate 終了日
     * @return メトリクスのリスト
     */
    List<TeamMetric> findByMetricDateBetween(LocalDate startDate, LocalDate endDate);

    List<TeamMetric> findByTeam_Event_EventId(String eventId);
}
