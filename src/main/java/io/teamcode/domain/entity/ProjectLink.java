package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 프로젝트에서 참고할 만한 링크 정보를 저장하는 Entity.
 */
@Data
@Entity
@Table(name="project_links")
public class ProjectLink {

    @GenericGenerator(
            name = "projectLinksSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_PROJECT_LINKS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "projectLinksSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_PROJECT_LINKS_PROJECT"))
    private Project project;

    @Basic(optional = false)
    @Column(name = "link_title", nullable = false, updatable = true)
    private String title;

    @Basic(optional = false)
    @Column(name = "link", nullable = false, updatable = true)
    private String link;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

}
