package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sak.metricstool.entity.ParticipantMetric;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParticipantMetricRepository extends JpaRepository<ParticipantMetric, Long> {
    /**
     * 特定の参加者に属するメトリクスを取得するメソッド。
     * @param participantId 参加者の一意識別子
     * @return メトリクスのリスト
     */
    List<ParticipantMetric> findByParticipant_ParticipantId(Long participantId);

    /**
     * 特定の日付範囲内の参加者メトリクスを取得するメソッド。
     * @param startDate 開始日
     * @param endDate 終了日
     * @return メトリクスのリスト
     */
    List<ParticipantMetric> findByMetricDateBetween(LocalDate startDate, LocalDate endDate);


    List<ParticipantMetric> findByParticipant_ParticipantIdIn(List<Long> participantIds);
}
