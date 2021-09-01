package io.teamcode.repository;

import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import io.teamcode.domain.entity.ci.Runner;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 4. 15..
 */
public interface JobRepository extends CrudRepository<Job, Long> {

    List<Job> findByPipeline(final Pipeline pipeline);

    List<Job> findByStatus(final PipelineStatus pipelineStatus);

    List<Job> findByRunner(final Runner runner);
}
