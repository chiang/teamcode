package io.teamcode.domain.entity.ci;

import io.teamcode.domain.entity.EntityState;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * 프로젝트에 설정된 CI 변수 정보. 빌드 또는 배포 시 이 값을 사용합니다. 값을 공유하거나 기본 설정 값을 Override 하거나 시스템 비밀번호 같은
 * 노출 시 보안에 민간한 정보를 보호하기 위해서 사용합니다.
 *
 * TODO GitLab 에서는 'aes-256-cbc' 를 사용해서 값을 암호화하는데 salt 를 가지고 한다. 우리도 이럴 필요가 있겠다...
 */
@Data
@Entity
@Table(name="ci_variables", indexes = {@Index(name="IDX_CI_VARIABLES", columnList = "variable_name")}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_CI_VARIABLES", columnNames = {"project_id", "variable_name"})})
public class CiVariable {

    @GenericGenerator(
            name = "ciVariablesSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_CI_VARIABLES"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "ciVariablesSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_CI_VARIABLES_PROJ"))
    private Project project;

    @Basic(optional = false)
    @Column(name = "variable_name", nullable = false)
    private String name;

    @Basic(optional = false)
    @Column(name = "variable_value", nullable = false)
    private String value;

    @Basic(optional = false)
    @Column(name = "is_secured", nullable = false)
    private Boolean secured = Boolean.FALSE;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_state", nullable = false)
    private EntityState entityState;

    @ManyToOne(optional = true)
    @JoinColumn(name="created_by", foreignKey = @ForeignKey(name = "FK_CI_VARIABLES_CREATOR"))
    private User createdBy;

    @ManyToOne(optional = true)
    @JoinColumn(name="updated_by", foreignKey = @ForeignKey(name = "FK_CI_VARIABLES_UPDATER"))
    private User updatedBy;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;
}
