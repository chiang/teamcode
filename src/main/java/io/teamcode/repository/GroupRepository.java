package io.teamcode.repository;

import io.teamcode.domain.entity.Group;
import io.teamcode.domain.entity.GroupType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 6..
 */
public interface GroupRepository extends CrudRepository<Group, Long> {

    Group findByPath(final String path);

    Long countByPath(final String path);

    List<Group> findByType(final GroupType type);

}
