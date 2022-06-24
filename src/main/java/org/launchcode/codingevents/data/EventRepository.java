package org.launchcode.codingevents.data;

import org.launchcode.codingevents.models.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Allows us to query classes of Event type out of the database
 * */
@Repository
public interface EventRepository extends CrudRepository<Event, Integer> {
}
