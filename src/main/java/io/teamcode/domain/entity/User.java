package io.teamcode.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.teamcode.common.Strings;
import io.teamcode.domain.ApplicationLayout;
import io.teamcode.domain.Theme;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 1. 18..
 */
@Data
@ToString(exclude = {"createdBy", "updatedBy"})
@EqualsAndHashCode(exclude = {"createdBy", "updatedBy"})
@Entity
@EntityListeners(value = { AuditingEntityListener.class })
@Table(name="tc_users", uniqueConstraints = {
        @UniqueConstraint(name = "UK_USERS_NAME", columnNames = {"name"}),
        @UniqueConstraint(name = "UK_USERS_EMAIL", columnNames = {"email"})})
public class User {

    @GenericGenerator(
            name = "userSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_TC_USERS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "userSequenceGenerator")
    private Long id;

    //실제 사용자 아이디
    //TODO 바꾸게 할 수 있나? 없나?
    @NotBlank
    @Basic(optional = false)
    @Column(name = "name", nullable = false, updatable = true)
    private String name;

    @Basic(optional = true)
    @Column(name = "full_name", nullable = true, updatable = true)
    private String fullName;

    @Getter(onMethod = @__( @JsonIgnore))
    @Basic(optional = false)
    @Column(name = "password", nullable = false, updatable = true)
    private String password;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, updatable = true)
    private UserRole userRole;

    @NotBlank
    @Email
    @Basic(optional = false)
    @Column(name = "email", nullable = false, updatable = true)
    private String email;

    @Basic(optional = true)
    @Column(name = "organization", nullable = true, updatable = true)
    private String organization;

    @Basic(optional = true)
    @Column(name = "bio", nullable = true, updatable = true)
    private String bio;

    /**
     * <p>알림 메시지를 받을 이메일 주소입니다. 만약 이 값이 <code>null</code> 이면 <code>email</code> 정보를 사용합니다.</p>
     * <p>이 말은 무조건 기본적으로 <code>email</code> 은 알림 용도로 사용한다는 의미입니다.</p>
     *
     */
    @Email
    @Basic(optional = true)
    @Column(name = "notification_email", nullable = true, updatable = true)
    private String notificationEmail;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, updatable = true)
    private UserState state;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, updatable = true)
    private Theme theme = Theme.CHARCOAL;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "layout", nullable = false, updatable = true)
    private ApplicationLayout layout = ApplicationLayout.FIXED;

    @Basic(optional = false)
    @Column(name = "sign_in_count", nullable = false, updatable = true)
    private Integer signInCount = 0;

    /**
     * 현재 로그인한 시간. 세션을 체크해야 하므로 여기서는 크게 다룰 부분이 아닌 것 같다?
     */
    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "current_sign_in_at", nullable = true, updatable = true)
    private Date currentSignInAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_sign_in_at", nullable = true, updatable = true)
    private Date lastSignInAt;

    /**
     * 비밀번호를 마지막으로 변경한 일시입니다.
     */
    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_password_modified_at", nullable = true, updatable = true)
    private Date lastPasswordModifiedAt;

    //TODO validation
    @Basic(optional = true)
    @Column(name = "current_sign_in_ip", nullable = true, updatable = true)
    private String currentSignInIp;

    //TODO validation
    @Basic(optional = true)
    @Column(name = "last_sign_in_ip", nullable = true, updatable = true)
    private String lastSignInIp;

    /**
     * 아바타를 설정한 경우 경로 정보를 저장합니다. 서버 상의 경로 밑 상대 경로로 갑니다. 주로 data/attachments 밑이 됩니다.
     *
     */
    @Basic(optional = true)
    @Column(name = "avatar_path", nullable = true, updatable = true)
    private String avatarPath;

    @CreatedDate
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    /**
     * 생성한 사람은 <code>null</code> 일 수 있다. 시스템 자체적으로 생성되는 경우도 있으니까.
     *
     */
    @ManyToOne(optional = true)
    @JoinColumn(name="created_by", foreignKey = @ForeignKey(name = "FK_USER_CREATOR"))
    private User createdBy;

    @Getter(onMethod = @__( @JsonIgnore))
    @ManyToOne(optional = true)
    @JoinColumn(name="updated_by", foreignKey = @ForeignKey(name = "FK_USER_UPDATER"))
    private User updatedBy;

    /**
     * 사용자 목록에서 현재 로그인한 사용자와 동일한지 여부를 표시합니다.
     */
    private transient boolean me;

    public void plusSignInCount() {
        this.signInCount++;
    }

    public String getAvatarFileName() {
        if (StringUtils.hasText(this.avatarPath)) {
            return Strings.getFileNameFromPath(this.avatarPath);
        }

        return null;
    }

    public boolean isAdmin() {

        return this.getUserRole() != null && this.userRole == UserRole.ROLE_ADMIN;
    }

}
