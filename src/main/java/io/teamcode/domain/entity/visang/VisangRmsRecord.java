package io.teamcode.domain.entity.visang;

import io.teamcode.domain.entity.Project;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 *
 * Visang RMS 에 연동하는 Entity. RMS 아이디를 기록하고 이를 기반으로 조회 기능을 제공합니다.
 *
 * TODO 나중에 ElasticSearch 로 변경합니다.
 */
@Data
@Entity
@Table(name="visang_rms_record", uniqueConstraints = {
        @UniqueConstraint(name = "UK_VISANG_RMS_REC", columnNames = {"project_id", "revision"})})
public class VisangRmsRecord {

    @GenericGenerator(
            name = "rmsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_VISANG_RMS_REC"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "rmsSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="project_id", foreignKey = @ForeignKey(name = "FK_VISANG_RMS_REC_PROJ"))
    private Project project;

    @Basic(optional = false)
    @Column(name = "revision", nullable = false, updatable = true)
    private Long revision;

    @Basic(optional = false)
    @Column(name = "rms_id", nullable = false, updatable = true)
    private String rmsId;

    @Basic(optional = false)
    @Column(name = "author_id", nullable = false, updatable = true)
    private String authorId;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_at", nullable = false, updatable = false)
    private Date lastModifiedAt;

    private transient List<VisangRmsRecordDetails> visangRmsRecordDetailses;

}
