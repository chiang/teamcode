package io.teamcode.repository;

import io.teamcode.domain.entity.IssueLabelPriority;
import io.teamcode.domain.entity.IssueLabelPriorityId;
import io.teamcode.domain.entity.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface IssueLabelPriorityRepository extends CrudRepository<IssueLabelPriority, IssueLabelPriorityId> {

    List<IssueLabelPriority> findByProject(final Project project);

    //@Query("SELECT ilp FROM IssueLabelPriority ilp WHERE issueLabelId = :issueLabelId")
    //IssueLabelPriority findByIssueLabelId(@Param("issueLabelId") final Long issueLabelId);
}
