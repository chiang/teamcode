package io.teamcode.repository;

import io.teamcode.domain.entity.AccountAuditEvent;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by chiang on 2017. 3. 5..
 */
public interface AccountAuditEventRepository extends CrudRepository<AccountAuditEvent, Long> {
}
