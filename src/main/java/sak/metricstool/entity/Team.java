package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * チームエンティティ
 */
@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId; // チームの一意識別子

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName; // チーム名

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event; // チームが所属するイベント

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("team")
    private List<TeamMetric> teamMetrics = new ArrayList<>(); // チームメトリクスのリスト

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("team")
    private List<Participant> participants = new ArrayList<>(); // チームに所属する参加者のリスト

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
