package io.teamcode.domain.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 6. 1..
 */
@Data
@Entity
@IdClass(IssueLabelPriorityId.class)
@Table(name="issue_label_priorities")
public class IssueLabelPriority {

    @Id
    @Column(name = "issue_label_id")
    private Long issueLabelId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne(optional = false)
    @JoinColumn(name="issue_label_id", referencedColumnName="id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="FK_ILP_ISSUE_LABEL"))
    private IssueLabel issueLabel;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", referencedColumnName="id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="FK_ILP_PROJ"))
    private Project project;

    //0 이상. 값이 null 이면
    //greater_than_or_equal_to: 0
    private Integer priority;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false, updatable = true)
    private Date updatedAt;

}
