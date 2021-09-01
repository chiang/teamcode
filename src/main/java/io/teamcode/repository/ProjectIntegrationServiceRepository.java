package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 4. 9..
 */
public interface ProjectIntegrationServiceRepository extends CrudRepository<ProjectIntegrationServiceSettings, Long> {

    ProjectIntegrationServiceSettings findByProjectAndKey(final Project project, final String key);

    List<ProjectIntegrationServiceSettings> findByProjectAndActiveAndCommitEvents(final Project project, final Boolean active, final Boolean commitEvents);

}
