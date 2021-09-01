package io.teamcode.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 6. 1..
 */
@Data
@Entity
@Table(name="issue_labels", uniqueConstraints = {@UniqueConstraint(name = "UK_ISSUE_LABELS", columnNames = {"project_id", "title"})})
public class IssueLabel {

    @GenericGenerator(
            name = "issueLabelsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_ISSUE_LABELS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "issueLabelsSequenceGenerator")
    private Long id;

    @Getter(onMethod = @__( @JsonIgnore))
    @ManyToOne
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_IL_PROJECT"))
    private Project project;

    @Basic(optional = false)
    @Column(name = "title", nullable = false, updatable = true)
    private String title;

    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true, length = 1000)
    private String description;

    /**
     * 6 Characters Hex Value #ffffff
     *
     */
    //TODO size validation
    @Basic(optional = true)
    @Column(name = "color", nullable = true, updatable = true, length = 7)
    private String color;

    @Basic(optional = false)
    @Column(name = "is_starred", nullable = false, updatable = true)
    private Boolean starred = Boolean.FALSE;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    @ManyToOne(optional = true)
    @JoinColumn(name="created_by", foreignKey = @ForeignKey(name = "FK_IL_CREATOR"))
    private User createdBy;

    @ManyToOne(optional = true)
    @JoinColumn(name="updated_by", foreignKey = @ForeignKey(name = "FK_IL_UPDATER"))
    private User updatedBy;

    private transient Long openedIssueCount;

    //t.boolean "template", default: false

    //project label or group label
    //t.string "type"

    //group 내에 속했다면 이게 되는 것이고 아니면 아니고
    //t.integer "group_id"
}
