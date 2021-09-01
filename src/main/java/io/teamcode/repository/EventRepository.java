package io.teamcode.repository;

import io.teamcode.domain.entity.Event;
import io.teamcode.domain.entity.EventAction;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 27..
 */
public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> findByProjectOrderByCreatedAtDesc(final Project project);

    int deleteByProjectAndActionAndAuthor(final Project project, final EventAction action, final User author);
}
