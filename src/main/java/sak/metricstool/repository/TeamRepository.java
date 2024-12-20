package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sak.metricstool.entity.Team;

import java.util.List;

/**
 * チーム情報に対するデータアクセス操作を提供するリポジトリインターフェース。
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 特定のイベントに属するチームを全て取得するメソッド。
     * @param eventId イベントの一意識別子
     * @return チームのリスト
     */
    List<Team> findByEvent_EventId(String eventId);
}
