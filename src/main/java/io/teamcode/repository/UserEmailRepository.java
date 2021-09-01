package io.teamcode.repository;

import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.UserEmail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 3..
 */
public interface UserEmailRepository extends CrudRepository<UserEmail, Long> {

    List<UserEmail> findByUser(final User user);

    Long countByEmail(final String email);

}
