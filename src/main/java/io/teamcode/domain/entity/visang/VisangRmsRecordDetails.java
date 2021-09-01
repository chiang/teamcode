package io.teamcode.domain.entity.visang;

import io.teamcode.common.Strings;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 변경된 파일 목록을 표시합니다. 삭제된 것은 보여주지 않습니다.
 *
 */
@Data
@Entity
@Table(name="visang_rms_record_details")
public class VisangRmsRecordDetails {

    @GenericGenerator(
            name = "rmsDetailsSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_VISANG_RMS_REC_DETAILS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "rmsDetailsSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="visang_rms_record_id", foreignKey = @ForeignKey(name = "FK_VISANG_RMS_REC_REC"))
    private VisangRmsRecord visangRmsRecord;

    @Basic(optional = false)
    @Column(name = "file_path", nullable = false, updatable = true)
    private String filePath;

    public String getFileName() {

        return Strings.getFileNameFromPath(this.filePath);
    }

}
