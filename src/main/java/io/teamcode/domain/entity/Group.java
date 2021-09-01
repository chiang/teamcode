package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiang on 2017. 2. 6..
 */
@Data
@Entity
@EntityListeners(value = { AuditingEntityListener.class })
@Table(name="groups")
public class Group {

    @GenericGenerator(
            name = "groupSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_GROUPS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "groupSequenceGenerator")
    private Long id;

    @NotBlank
    @Basic(optional = false)
    @Column(name = "name", nullable = false, updatable = true)
    private String name;

    @NotBlank
    @Basic(optional = false)
    @Column(name = "path", nullable = false, updatable = true)
    private String path;

    @Basic(optional = true)
    @Column(name = "description", nullable = true, updatable = true)
    private String description;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = true)
    private GroupType type;

    /**
     * Group 의 Owner 는 여러 명이 될 수 있으므로 이 값은 그냥 생성자로서만?
     *
     */
    @ManyToOne(optional = false)
    @JoinColumn(name="owner_id", foreignKey = @ForeignKey(name = "FK_GROUPS_OWNER"))
    private User owner;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_level", nullable = false, updatable = true)
    private Visibility visibility = Visibility.INTERNAL;

    @CreatedDate
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

    //private Date deletedAt;

    //t.string "type"
    //t.string "avatar"
    //t.boolean "share_with_group_lock", default: false
    //t.integer "visibility_level", default: 20, null: false
    //t.boolean "request_access_enabled", default: false, null: false
    //t.datetime "deleted_at"
}
