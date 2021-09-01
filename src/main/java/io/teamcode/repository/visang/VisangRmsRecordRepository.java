package io.teamcode.repository.visang;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.visang.VisangRmsRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 4. 3..
 */
public interface VisangRmsRecordRepository extends CrudRepository<VisangRmsRecord, Long> {

    VisangRmsRecord findByProjectAndRevision(final Project project, final Long revision);

    List<VisangRmsRecord> findByRmsId(final String rmsId);
}
