package sak.metricstool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sak.metricstool.entity.Event;
import sak.metricstool.entity.Participant;
import sak.metricstool.entity.Team;
import sak.metricstool.repository.TeamRepository;
import sak.metricstool.exception.ResourceNotFoundException;

import java.util.List;

/**
 * チーム管理に関連するビジネスロジックを提供するサービスクラス。
 */
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final EventService eventService; // イベント情報の取得に使用

    @Autowired
    public TeamService(TeamRepository teamRepository, EventService eventService) {
        this.teamRepository = teamRepository;
        this.eventService = eventService;
    }

    /**
     * 全てのチームを取得します。
     * @return チームのリスト
     */
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * イベントIDに基づいて特定のイベントに属するチームを取得します。
     * @param eventId イベントの一意識別子
     * @return チームのリスト
     */
    public List<Team> getTeamsByEventId(String eventId) {
        return teamRepository.findByEvent_EventId(eventId);
    }

    /**
     * チームIDに基づいて特定のチームを取得します。
     * @param teamId チームの一意識別子
     * @return 該当するチーム
     * @throws ResourceNotFoundException チームが見つからない場合
     */
    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
    }

    /**
     * 新しいチームを作成します。
     * @param team チームオブジェクト
     * @return 作成されたチーム
     */
    public Team createTeam(Team team) {
        // 所属イベントが存在するか確認
        String eventId = team.getEvent().getEventId();
        Event event = eventService.getEventById(eventId);
        team.setEvent(event);
        return teamRepository.save(team);
    }

    /**
     * 既存のチームを更新します。
     * @param teamId チームの一意識別子
     * @param teamDetails 更新内容を含むチームオブジェクト
     * @return 更新されたチーム
     * @throws ResourceNotFoundException チームが見つからない場合
     */
    public Team updateTeam(Long teamId, Team teamDetails) {
        Team team = getTeamById(teamId);
        team.setTeamName(teamDetails.getTeamName());

        // 所属イベントの更新が必要な場合
        if (teamDetails.getEvent() != null) {
            String newEventId = teamDetails.getEvent().getEventId();
            Event newEvent = eventService.getEventById(newEventId);
            team.setEvent(newEvent);
        }

        return teamRepository.save(team);
    }

    /**
     * チームを削除します。
     * @param teamId チームの一意識別子
     * @throws ResourceNotFoundException チームが見つからない場合
     */
    public void deleteTeam(Long teamId) {
        Team team = getTeamById(teamId);
        teamRepository.delete(team);
    }


    /**
     * 指定されたチームIDに属する参加者を取得します。
     * @param teamId チームの一意識別子
     * @return 参加者のリスト
     */
    public List<Participant> getParticipantsByTeamId(Long teamId) {
        Team team = getTeamById(teamId);
        return team.getParticipants();
    }

}
