package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.Visibility;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 6..
 */
public interface ProjectRepository extends CrudRepository<Project, Long> {

    Project findByName(final String name);

    Project findByPath(final String path);

    List<Project> findAll(Sort sort);

    List<Project> findByNameContaining(final String name, Sort sort);

    List<Project> findByVisibility(final Visibility visibility);

    List<Project> findByArchived(final Boolean archived);
}
