package io.teamcode.domain.entity;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 사용자의 Audit Event 를 기록합니다. 현재는 로그인만 처리합니다. 나중에 내용이 있다면 details 에 넣으면 좋겠습니다.
 *
 */
@Data
@Entity
@EntityListeners(value = { AuditingEntityListener.class })
@Table(name="account_audit_events")
public class AccountAuditEvent {

    @GenericGenerator(
            name = "accountAuditEventSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_ACCOUNT_AUDIT_EVENTS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "accountAuditEventSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "FK_ACCOUNT_AUDIT_EVENTS_USER"))
    private User user;

    /**
     * 로그인 이벤트일 경우만 해당?
     *
     */
    @Basic(optional = true)
    @Column(name = "ip_addr", nullable = true, updatable = false)
    private String ipAddress;

    /**
     * UserAgent Library 에서 제공하는 UserAgent Object 의 ID
     */
    @Basic(optional = true)
    @Column(name = "user_agent_id", nullable = true, updatable = true)
    private Integer userAgentId;

    @Basic(optional = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "browser", nullable = true, updatable = true)
    private Browser browser;

    @Basic(optional = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "os", nullable = true, updatable = true)
    private OperatingSystem operatingSystem;

    @Basic(optional = true)
    @Column(name = "user_agent_string", nullable = true, updatable = true, length = 500)
    private String userAgentString;

    @CreatedDate
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

}
