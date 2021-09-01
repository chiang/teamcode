package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 이슈를 등록할 때 사용하는 마일스톤 정보.
 */
@Data
@Entity
@Table(name="milestones", uniqueConstraints = {@UniqueConstraint(name = "UK_MILESTONES", columnNames = {"project_id", "title"})})
public class Milestone {

    @GenericGenerator(
            name = "milestonesSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_MILESTONES"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "milestonesSequenceGenerator")
    private Long id;

    @NotBlank
    @Basic(optional = false)
    @Column(name = "title", nullable = false, updatable = true)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_MILESTONES_PROJ"))
    private Project project;

    //text type
    /**
     * 설명을 굳이 달 필요가 있는가? 버전 같은 것은 그냥 명시만 해도 되니까. 그래서 optional=true 임.
     */
    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true)
    private String description;

    //start, due 는 모두 없어도 됨.
    //type date
    @Basic(optional = true)
    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = true, updatable = false)
    private Date startDate;

    //type date
    @Basic(optional = true)
    @Temporal(TemporalType.DATE)
    @Column(name = "due_date", nullable = true, updatable = false)
    private Date dueDate;

    @NotNull
    @Basic(optional = false)
    @Column(name = "state", nullable = false, updatable = true)
    private MilestoneState state;

    @NotNull
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    @ManyToOne(optional = true)
    @JoinColumn(name="created_by", foreignKey = @ForeignKey(name = "FK_MILESTONES_CREATOR"))
    private User createdBy;

    @ManyToOne(optional = true)
    @JoinColumn(name="updated_by", foreignKey = @ForeignKey(name = "FK_MILESTONES_UPDATER"))
    private User updatedBy;

    /**
     * 폼에서 전송된 값을 저장하는 용도. 이 값을 Date 로 변환해서 저장하게 됩니다. 포멧은 yyyy-MM-dd 입니다.
     */
    private transient String startDateStr;

    /**
     * 폼에서 전송된 값을 저장하는 용도. 이 값을 Date 로 변환해서 저장하게 됩니다. 포멧은 yyyy-MM-dd 입니다.
     */
    private transient String dueDateStr;

    public String toReference() {
        /*def to_reference(from_project = nil, format: :iid, full: false)
        format_reference = milestone_format_reference(format)
        reference = "#{self.class.reference_prefix}#{format_reference}"
        "#{project.to_reference(from_project, full: full)}#{reference}"
        end*/
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"#!\">").append(getTitle()).append("</a>");
        //TODO gitlab 처럼 Template 을 가동하는 것은 ?

        return builder.toString();
    }

    //t.integer "iid"
}
