package sak.metricstool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sak.metricstool.entity.Participant;
import sak.metricstool.entity.Team;
import sak.metricstool.exception.ResourceNotFoundException;
import sak.metricstool.repository.ParticipantRepository;

import java.util.List;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TeamService teamService; // チーム情報の取得に使用

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository, TeamService teamService) {
        this.participantRepository = participantRepository;
        this.teamService = teamService;
    }

    /**
     * 全ての参加者を取得します。
     * @return 参加者のリスト
     */
    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    /**
     * 特定のチームに属する参加者を取得します。
     * @param teamId チームの一意識別子
     * @return 参加者のリスト
     */
    public List<Participant> getParticipantsByTeamId(Long teamId) {
        return participantRepository.findByTeam_TeamId(teamId);
    }

    /**
     * 参加者IDに基づいて特定の参加者を取得します。
     * @param participantId 参加者の一意識別子
     * @return 該当する参加者
     * @throws ResourceNotFoundException 参加者が見つからない場合
     */
    public Participant getParticipantById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));
    }

    /**
     * 新しい参加者を作成します。
     * @param participant 参加者オブジェクト
     * @return 作成された参加者
     */
    public Participant createParticipant(Participant participant) {
        // 所属チームが存在するか確認
        Long teamId = participant.getTeam().getTeamId();
        Team team = teamService.getTeamById(teamId);
        participant.setTeam(team);
        return participantRepository.save(participant);
    }

    /**
     * 既存の参加者を更新します。
     * @param participantId 参加者の一意識別子
     * @param participantDetails 更新内容を含む参加者オブジェクト
     * @return 更新された参加者
     * @throws ResourceNotFoundException 参加者が見つからない場合
     */
    public Participant updateParticipant(Long participantId, Participant participantDetails) {
        Participant participant = getParticipantById(participantId);
        participant.setParticipantName(participantDetails.getParticipantName());
        participant.setParticipantGitlabId(participantDetails.getParticipantGitlabId());

        // GitLabメールアドレスの更新（ユニーク制約に注意）
        participant.setParticipantGitlabEmail(participantDetails.getParticipantGitlabEmail());

        // 所属チームの更新が必要な場合
        if (participantDetails.getTeam() != null) {
            Long newTeamId = participantDetails.getTeam().getTeamId();
            Team newTeam = teamService.getTeamById(newTeamId);
            participant.setTeam(newTeam);
        }

        return participantRepository.save(participant);
    }

    /**
     * 参加者を削除します。
     * @param participantId 参加者の一意識別子
     * @throws ResourceNotFoundException 参加者が見つからない場合
     */
    public void deleteParticipant(Long participantId) {
        Participant participant = getParticipantById(participantId);
        participantRepository.delete(participant);
    }

}
