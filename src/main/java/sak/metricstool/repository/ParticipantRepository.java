package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sak.metricstool.entity.Participant;
import java.util.List;
import java.util.Optional;

/**
 * 参加者情報に対するデータアクセス操作を提供するリポジトリインターフェース。
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    /**
     * 特定のチームに属する参加者を全て取得するメソッド。
     * @param teamId チームの一意識別子
     * @return 参加者のリスト
     */
    List<Participant> findByTeam_TeamId(Long teamId);

    /**
     * GitLabメールアドレスに基づいて参加者を取得するメソッド。
     * @param email GitLabメールアドレス
     * @return 参加者のオプショナルオブジェクト
     */
    Optional<Participant> findByParticipantGitlabEmail(String email);
}
