package sak.metricstool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * イベントエンティティ
 */
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @Column(name = "event_id")
    private String eventId; // イベントの一意識別子（UUIDなど）

    @Column(name = "event_name", nullable = false, length = 100)
    private String eventName; // イベント名

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // イベント開始日時

    @Column(name = "end_date")
    private LocalDateTime endDate; // イベント終了日時（未定の場合はnull）

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("event")
    private List<Team> teams = new ArrayList<>(); // イベントに所属するチームのリスト

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
