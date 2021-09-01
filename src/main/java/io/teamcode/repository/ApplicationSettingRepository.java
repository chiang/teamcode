package io.teamcode.repository;

import io.teamcode.domain.entity.ApplicationSetting;
import org.springframework.data.repository.CrudRepository;

/**
 * 항상 Entity 는 하나만 저장됩니다.
 */
public interface ApplicationSettingRepository extends CrudRepository<ApplicationSetting, Long> {

}
