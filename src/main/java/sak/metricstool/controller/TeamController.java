package sak.metricstool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sak.metricstool.entity.Team;
import sak.metricstool.service.TeamService;

import java.util.List;

/**
 * チーム管理に関連するREST APIエンドポイントを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 全てのチームを取得します。
     * @return チームのリスト
     */
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    /**
     * 特定のイベントに属するチームを取得します。
     * @param eventId イベントの一意識別子
     * @return チームのリスト
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Team>> getTeamsByEventId(@PathVariable String eventId) {
        List<Team> teams = teamService.getTeamsByEventId(eventId);
        return ResponseEntity.ok(teams);
    }

    /**
     * 特定のチームを取得します。
     * @param teamId チームの一意識別子
     * @return 該当するチーム
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long teamId) {
        Team team = teamService.getTeamById(teamId);
        return ResponseEntity.ok(team);
    }

    /**
     * 新しいチームを作成します。
     * @param team チームオブジェクト
     * @return 作成されたチーム
     */
    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team createdTeam = teamService.createTeam(team);
        return ResponseEntity.status(201).body(createdTeam);
    }

    /**
     * 既存のチームを更新します。
     * @param teamId チームの一意識別子
     * @param teamDetails 更新内容を含むチームオブジェクト
     * @return 更新されたチーム
     */
    @PutMapping("/{teamId}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long teamId, @RequestBody Team teamDetails) {
        Team updatedTeam = teamService.updateTeam(teamId, teamDetails);
        return ResponseEntity.ok(updatedTeam);
    }

    /**
     * チームを削除します。
     * @param teamId チームの一意識別子
     * @return 空のレスポンス
     */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }
}
