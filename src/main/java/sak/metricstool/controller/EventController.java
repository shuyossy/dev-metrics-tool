package sak.metricstool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sak.metricstool.entity.Event;
import sak.metricstool.service.EventService;

import java.util.List;

/**
 * イベント管理に関連するREST APIエンドポイントを提供するコントローラー。
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 全てのイベントを取得します。
     * @return イベントのリスト
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * 特定のイベントを取得します。
     * @param eventId イベントの一意識別子
     * @return 該当するイベント
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable String eventId) {
        Event event = eventService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * 新しいイベントを作成します。
     * @param event イベントオブジェクト
     * @return 作成されたイベント
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(201).body(createdEvent);
    }

    /**
     * 既存のイベントを更新します。
     * @param eventId イベントの一意識別子
     * @param eventDetails 更新内容を含むイベントオブジェクト
     * @return 更新されたイベント
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@PathVariable String eventId, @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(eventId, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * イベントを削除します。
     * @param eventId イベントの一意識別子
     * @return 空のレスポンス
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
