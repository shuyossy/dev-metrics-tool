package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 参加者エンティティ
 */
@Entity
@Table(name = "participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long participantId; // 参加者の一意識別子

    @Column(name = "participant_name", nullable = false, length = 100)
    private String participantName; // 参加者名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonBackReference
    private Team team; // 参加者が所属するチーム

    @Column(name = "participant_gitlab_id", nullable = false)
    private String participantGitlabId; // GitLabでのユーザーID

    @Column(name = "participant_gitlab_email", nullable = false, unique = true)
    private String participantGitlabEmail; // GitLabでのユーザーEメール

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("participant")
    private List<ParticipantMetric> participantMetrics = new ArrayList<>(); // 参加者メトリクスのリスト

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // レコード作成日時

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // レコード更新日時

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
