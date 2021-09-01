package io.teamcode.repository;

import io.teamcode.domain.entity.Milestone;
import io.teamcode.domain.entity.MilestoneState;
import io.teamcode.domain.entity.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface MilestoneRepository extends CrudRepository<Milestone, Long> {

    int countByProject(final Project project);

    int countByProjectAndState(final Project project, final MilestoneState milestoneState);

    List<Milestone> findByProject(final Project project);

    List<Milestone> findByProjectAndState(final Project project, final MilestoneState milestoneState);
}
