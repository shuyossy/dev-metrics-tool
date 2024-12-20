package sak.metricstool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sak.metricstool.entity.Event;
import sak.metricstool.repository.EventRepository;
import sak.metricstool.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * 全てのイベントを取得します。
     * @return イベントのリスト
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * イベントIDに基づいてイベントを取得します。
     * @param eventId イベントの一意識別子
     * @return 該当するイベント
     * @throws ResourceNotFoundException イベントが見つからない場合
     */
    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    /**
     * 新しいイベントを作成します。
     * @param event イベントオブジェクト
     * @return 作成されたイベント
     */
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    /**
     * 既存のイベントを更新します。
     * @param eventId イベントの一意識別子
     * @param eventDetails 更新内容を含むイベントオブジェクト
     * @return 更新されたイベント
     * @throws ResourceNotFoundException イベントが見つからない場合
     */
    public Event updateEvent(String eventId, Event eventDetails) {
        Event event = getEventById(eventId);
        event.setEventName(eventDetails.getEventName());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        return eventRepository.save(event);
    }

    /**
     * イベントを削除します。
     * @param eventId イベントの一意識別子
     * @throws ResourceNotFoundException イベントが見つからない場合
     */
    public void deleteEvent(String eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
    }
}
