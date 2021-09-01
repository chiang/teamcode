package io.teamcode.domain.entity;

import io.teamcode.domain.Auditable;
import io.teamcode.domain.AuditableType;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 6. 1..
 */
@Data
@Entity
@Table(name="issues")
public class Issue implements Auditable {

    @GenericGenerator(
            name = "issuesSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_ISSUES"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "issuesSequenceGenerator")
    private Long id;

    //sequence in repository
    @Basic(optional = false)
    @Column(name = "internal_id", nullable = false, updatable = false)
    private Long internalId;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_ISSUES_PROJECT"))
    private Project project;

    @NotBlank
    @Basic(optional = false)
    @Column(name = "title", nullable = false, updatable = true)
    private String title;

    //text type
    @NotBlank
    @Basic(optional = false)
    @Column(name = "description", nullable = false, updatable = true)
    private String description;

    @Basic(optional = false)
    @Column(name = "state", nullable = false, updatable = true)
    private IssueState state;

    @ManyToOne(optional = true)
    @JoinColumn(name="milestone_id", foreignKey = @ForeignKey(name = "FK_ISSUES_MILESTONE"))
    private Milestone milestone;

    @ManyToOne(optional = true)
    @JoinColumn(name="assignee_id", foreignKey = @ForeignKey(name = "FK_ISSUES_ASSIGNEE"))
    private User assignee;

    @ManyToOne(optional = false)
    @JoinColumn(name="author_id", foreignKey = @ForeignKey(name = "FK_ISSUES_AUTHOR"))
    private User author;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    @ManyToOne(optional = true)
    @JoinColumn(name="updated_by", foreignKey = @ForeignKey(name = "FK_ISSUES_UPDATER"))
    private User updatedBy;

    private transient AuditableType auditableType = AuditableType.ISSUE;

}
