package io.teamcode.repository.visang;

import io.teamcode.domain.entity.visang.VisangRmsRecord;
import io.teamcode.domain.entity.visang.VisangRmsRecordDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by chiang on 2017. 4. 3..
 */
public interface VisangRmsRecordDetailsRepository extends CrudRepository<VisangRmsRecordDetails, Long> {

    List<VisangRmsRecordDetails> findByVisangRmsRecord(final VisangRmsRecord visangRmsRecord);
}
