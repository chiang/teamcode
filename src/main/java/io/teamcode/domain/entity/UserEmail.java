package io.teamcode.domain.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 사용자의 이메일 정보를 표현하는 Entity.
 *
 * Created by chiang on 2017. 2. 3..
 */
@Data
@Entity
@Table(name="user_emails", uniqueConstraints = {@UniqueConstraint(name = "UK_USER_EMAILS", columnNames = {"email"})})
public class UserEmail {

    @GenericGenerator(
            name = "userEmailSequenceGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "SEQ_USER_EMAILS"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    @Id
    @GeneratedValue(generator = "userEmailSequenceGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", foreignKey = @ForeignKey(name = "FK_USER_EMAILS"))
    private User user;

    @Email
    @Basic(optional = true)
    @Column(name = "email", nullable = true, updatable = true)
    private String email;

    @NotNull
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Basic(optional = true)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = true, updatable = true)
    private Date updatedAt;

}
