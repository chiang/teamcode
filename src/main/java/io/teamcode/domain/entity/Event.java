package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;

/**
 *  t.string "target_type"
 t.integer "target_id"
 t.string "title"
 t.text "data"
 t.integer "project_id"
 t.datetime "created_at"
 t.datetime "updated_at"
 t.integer "action"
 t.integer "author_id"
 *
 * 사용자 Activity 가 여기에 저장됩니다.
 */
@Data
@Entity
@EntityListeners(value = { AuditingEntityListener.class })
@Table(name="events")
public class Event {

    @GenericGenerator(
            name = "eventsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_EVENTS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "eventsSequenceGenerator")
    private Long id;

    @Basic(optional = true)
    @Column(name = "entity_id", nullable = true, updatable = true)
    private Long entityId;

    //class name
    @Basic(optional = true)
    @Column(name = "entity_type", nullable = true, updatable = true)
    private String entityType;

    @Basic(optional = true)
    @Column(name = "title", nullable = true, updatable = true)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_EVENTS_PROJECT"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name="author_id", foreignKey = @ForeignKey(name = "FK_EVENTS_AUTHOR"))
    private User author;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "event_action", nullable = false, updatable = false)
    private EventAction action;

    @CreatedDate
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    private transient Object targetEntity;

    public String getEventActionName() {
        if (!StringUtils.hasText(getEntityType())) {

            return new StringBuilder("프로젝트 ").append(getProject().getName()).append(" 를 생성했습니다.").toString();
        }

        return "";
    }

    public String getActionName() {
        switch(getAction()) {
            case JOINED:
                return "joined";

            default:
                return "";
        }
    }

}
