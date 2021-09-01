package io.teamcode.domain.entity.ci;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Runner 는 많은 레코드가 쌓일 일이 없기 때문에 특별히 Index 를 생성하지 않습니다.
 */
@Data
@Entity
@Table(name="ci_runners", uniqueConstraints = {
        @UniqueConstraint(name = "UK_RUNNERS_TOKEN", columnNames = {"token"})})
public class Runner {

    /**
     * LAST_CONTACT_TIME = 1.hour.ago
     */
    private static final int LAST_CONTACT_TIME = 1000 * 60 * 60;

    @GenericGenerator(
            name = "runnersSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_CI_RUNNERS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "runnersSequenceGenerator")
    private Long id;

    @Basic(optional = true)
    @Column(name = "name", nullable = true, updatable = true)
    private String name;

    /**
     * Runner 가 등록될 때 전달하는 <code>token</code> 값입니다.
     * 이 값은 서버에서 생성한 token 을 Runner 에 등록하고 사용하는 것인데, 이 값은 Ruby 의 Devise.friendly_token 값과 동일합니다
     * (derived Gitlab Architecture). 이 값은 길이가 기본 20 입니다.
     *
     * 또한 이 값은 중복될 수 없습니다 (SHA1 으로 생성하니까 중복될 수 없습니다). 그리고 업데이트 될 수도 없습니다. 왜냐면,
     * 이 값은 키로 사용되기 때문입니다.
     *
     *
     */
    @Size(min = 20, max = 20)
    @Basic(optional = false)
    @Column(name = "token", nullable = false, updatable = false, length = 20)
    private String token;

    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true)
    private String description;

    @Basic(optional = true)
    @Column(name = "version", nullable = true, updatable = true)
    private String version;

    @Basic(optional = true)
    @Column(name = "revision", nullable = true, updatable = true)
    private String revision;

    @Basic(optional = true)
    @Column(name = "platform", nullable = true, updatable = true)
    private String platform;

    @Basic(optional = true)
    @Column(name = "architecture", nullable = true, updatable = true)
    private String architecture;

    @Basic(optional = false)
    @Column(name = "is_active", nullable = false, updatable = true)
    private Boolean active = Boolean.TRUE;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "contacted_at", nullable = false, updatable = true)
    private Date contactedAt;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    public boolean isOnline() {
        if (this.contactedAt == null)
            return false;

        return DateTime.now().isBefore(new DateTime(this.contactedAt).plusMillis(LAST_CONTACT_TIME));
    }

    public String getDisplayName() {
        if (StringUtils.hasText(description))
            return this.description;
        else
            return this.getShortSha();
    }

    public String getShortSha() {
        if (StringUtils.hasText(token))
            return token.substring(0, 8);

        //token이 notNull 이기 때문에 여기에 도달할 수 없다.
        return "";
    }

    /*
    t.string "token"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "description"
    t.datetime "contacted_at"
    t.boolean "active", default: true, null: false
    t.boolean "is_shared", default: false
    t.string "name"
    t.string "version"
    t.string "revision"
    t.string "platform"
    t.string "architecture"
    t.boolean "run_untagged", default: true, null: false
    t.boolean "locked", default: false, null: false
     */
}

