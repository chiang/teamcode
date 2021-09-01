package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 1. 18..
 */
@Data
@Entity
@Table(name="project_members")
public class ProjectMember {

    @GenericGenerator(
            name = "projectMembersSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_PROJECT_MEMBERS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "projectMembersSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_PROJECT_MEMBERS_PROJECT"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_PROJECT_MEMBERS_PROJECT_USER"))
    private User user;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false, updatable = true)
    private ProjectRole role;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

}
