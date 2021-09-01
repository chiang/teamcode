package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectLink;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 5. 13..
 */
public interface ProjectLinkRepository extends CrudRepository<ProjectLink, Long> {

    List<ProjectLink> findByProject(final Project project);
}
