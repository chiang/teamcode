package io.teamcode.repository;

import io.teamcode.domain.entity.Issue;
import io.teamcode.domain.entity.IssueState;
import io.teamcode.domain.entity.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 */
public interface IssueRepository extends CrudRepository<Issue, Long> {

    Long countByProjectAndState(final Project project, final IssueState state);

    Issue findByProjectAndInternalId(final Project project, final Long internalId);

    List<Issue> findByProject(final Project project, final Pageable pageable);

    @Query("SELECT MAX(internalId) FROM Issue WHERE project = :project")
    Long getMaxInternalIdByProject(@Param("project") final Project project);

    //Issue findByInternalId(final Long internalId);

}
