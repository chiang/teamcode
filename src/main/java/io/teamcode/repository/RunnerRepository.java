package io.teamcode.repository;

import io.teamcode.domain.entity.ci.Runner;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by chiang on 2017. 4. 11..
 */
public interface RunnerRepository extends CrudRepository<Runner, Long> {

    Runner findByToken(final String token);

}
