package io.teamcode.service;

import io.teamcode.domain.entity.Event;
import io.teamcode.domain.entity.EventAction;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import io.teamcode.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Commit 은 기록하지 않는다. Commit 은 별도 메뉴에서 다 보이기 때문이다.
 *
 * Created by chiang on 2017. 2. 27..
 */
@Service
@Transactional(readOnly = true)
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    EventRepository eventRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public void createProject(final Project project) {

        createEvent(project, userService.getCurrentUser(), EventAction.CREATED);
    }

    public void joinProject(final Project project, final User user) {

        createEvent(project, user, EventAction.JOINED);
    }

    @Transactional
    public void removeJoinEvent(final Project project, final User user) {

        eventRepository.deleteByProjectAndActionAndAuthor(project, EventAction.JOINED, user);
    }

    @Transactional
    public void createEvent(final Project project, final User user, final EventAction eventAction) {
        Event event = new Event();
        event.setProject(project);
        event.setAction(eventAction);

        if (eventAction == EventAction.JOINED) {
            event.setEntityId(user.getId());
            //io.teamcode.domain.entity.User 와 같이 들어감.
            event.setEntityType(user.getClass().getCanonicalName());
        }

        event.setAuthor(userService.getCurrentUser());

        eventRepository.save(event);
        logger.debug("새로운 이벤트를 기록했습니다. 이벤트 타입: {}, 액션: {}", (StringUtils.hasText(event.getEntityType()) ? event.getEntityType() : "Project"), event.getAction());
    }

    public List<Event> getEventsByProject(final String projectPath) {
        Project project = projectService.getByPath(projectPath);

        List<Event> events = eventRepository.findByProjectOrderByCreatedAtDesc(project);
        for (Event event: events) {
            if (StringUtils.hasText(event.getEntityType())) {
                try {
                    event.setTargetEntity(entityManager.find(Class.forName(event.getEntityType()), event.getEntityId()));
                } catch (ClassNotFoundException e) {
                    logger.warn("타켓 엔티티 타입 '{}' 로 정의된 클래스를 찾을 수 없습니다.", event.getEntityType());
                }
            }
        }

        return events;
    }
}
