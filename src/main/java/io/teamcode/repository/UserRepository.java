package io.teamcode.repository;

import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.UserRole;
import io.teamcode.domain.entity.UserState;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 1. 19..
 */
public interface UserRepository extends CrudRepository<User, Long> {

    User findByName(final String name);

    List<User> findByUserRole(final UserRole userRole, Sort sort);

    List<User> findByState(final UserState userState, Sort sort);

    int countByState(final UserState userState);

    int countByUserRole(final UserRole userRole);

    //@Query("SELECT COUNT(user) FROM user WHERE id NOT IN (SELECT DISTINCT(user_id) FROM members WHERE user_id IS NOT NULL AND requested_at IS NULL)'")
    //int countWithoutProjects();

}
