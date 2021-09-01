package io.teamcode.repository;

import io.teamcode.domain.entity.IssueLabel;
import io.teamcode.domain.entity.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface IssueLabelRepository extends CrudRepository<IssueLabel, Long> {

    Long countByProject(final Project project);

    List<IssueLabel> findByProject(final Project project);

}
