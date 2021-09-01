package io.teamcode.domain.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by chiang on 2017. 6. 3..
 */
@Data
@Entity
@IdClass(IssueLabelMapId.class)
@Table(name="issue_label_map")
public class IssueLabelMap {

    @Id
    @Column(name = "issue_id")
    private Long issueId;

    @Id
    @Column(name = "issue_label_id")
    private Long issueLabelId;

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @ManyToOne(optional = false)
    @JoinColumn(name="issue_id", referencedColumnName="id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="FK_ILM_ISSUE"))
    private Issue issue;

    @ManyToOne(optional = false)
    @JoinColumn(name="issue_label_id", referencedColumnName="id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="FK_ILM_ISSUE_LABEL"))
    private IssueLabel issueLabel;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", referencedColumnName="id", insertable = false, updatable = false, foreignKey = @ForeignKey(name="FK_ILM_PROJ"))
    private Project project;

    @NotNull
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

}
