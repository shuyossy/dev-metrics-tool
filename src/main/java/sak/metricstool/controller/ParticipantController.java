package sak.metricstool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sak.metricstool.entity.Participant;
import sak.metricstool.service.ParticipantService;

import java.util.List;

/**
 * 参加者管理に関連するREST APIエンドポイントを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @Autowired
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * 全ての参加者を取得します。
     * @return 参加者のリスト
     */
    @GetMapping
    public ResponseEntity<List<Participant>> getAllParticipants() {
        List<Participant> participants = participantService.getAllParticipants();
        return ResponseEntity.ok(participants);
    }

    /**
     * 特定のチームに属する参加者を取得します。
     * @param teamId チームの一意識別子
     * @return 参加者のリスト
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Participant>> getParticipantsByTeamId(@PathVariable Long teamId) {
        List<Participant> participants = participantService.getParticipantsByTeamId(teamId);
        return ResponseEntity.ok(participants);
    }

    /**
     * 特定の参加者を取得します。
     * @param participantId 参加者の一意識別子
     * @return 該当する参加者
     */
    @GetMapping("/{participantId}")
    public ResponseEntity<Participant> getParticipantById(@PathVariable Long participantId) {
        Participant participant = participantService.getParticipantById(participantId);
        return ResponseEntity.ok(participant);
    }

    /**
     * 新しい参加者を作成します。
     * @param participant 参加者オブジェクト
     * @return 作成された参加者
     */
    @PostMapping
    public ResponseEntity<Participant> createParticipant(@RequestBody Participant participant) {
        Participant createdParticipant = participantService.createParticipant(participant);
        return ResponseEntity.status(201).body(createdParticipant);
    }

    /**
     * 既存の参加者を更新します。
     * @param participantId 参加者の一意識別子
     * @param participantDetails 更新内容を含む参加者オブジェクト
     * @return 更新された参加者
     */
    @PutMapping("/{participantId}")
    public ResponseEntity<Participant> updateParticipant(@PathVariable Long participantId, @RequestBody Participant participantDetails) {
        Participant updatedParticipant = participantService.updateParticipant(participantId, participantDetails);
        return ResponseEntity.ok(updatedParticipant);
    }

    /**
     * 参加者を削除します。
     * @param participantId 参加者の一意識別子
     * @return 空のレスポンス
     */
    @DeleteMapping("/{participantId}")
    public ResponseEntity<Void> deleteParticipant(@PathVariable Long participantId) {
        participantService.deleteParticipant(participantId);
        return ResponseEntity.noContent().build();
    }
}
