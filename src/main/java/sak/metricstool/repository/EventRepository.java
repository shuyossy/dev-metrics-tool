package sak.metricstool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sak.metricstool.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    // eventId が主キーのため、String をIDタイプとして指定
}
