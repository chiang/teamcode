package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ci.CiVariable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 *
 */
public interface CiVariableRepository extends CrudRepository<CiVariable, Long> {

    List<CiVariable> findByProject(final Project project);

    CiVariable findByProjectAndName(final Project project, final String name);
}
