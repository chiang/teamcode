package io.teamcode.domain.entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * 시스템 설정 정보를 저장하는 Entity 입니다.
 *
 * Created by chiang on 2017. 4. 11..
 */
@Data
@Entity
@Table(name="application_settings")
public class ApplicationSetting {

    @GenericGenerator(
            name = "applicationSettingSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_APP_SETTINGS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "applicationSettingSequenceGenerator")
    private Long id;

    @Basic(optional = false)
    @Column(name = "runners_registration_token", nullable = false, updatable = true)
    private String runnersRegistrationToken;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;
}
