package io.teamcode.repository;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 8..
 */
public interface PipelineRepository extends CrudRepository<Pipeline, Long> {

    int countByProject(final Project project);

    int countByProjectAndStatus(final Project project, PipelineStatus status);

    int countByProjectAndStatusIn(final Project project, final Collection<PipelineStatus> status);

    Page<Pipeline> findByProject(final Project project, final Pageable pageable);

    Page<Pipeline> findByProjectAndStatus(final Project project, final PipelineStatus status, final Pageable pageable);

    Page<Pipeline> findByProjectAndStatusIn(final Project project, final Collection<PipelineStatus> status, final Pageable pageable);

}
