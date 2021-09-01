package io.teamcode.service;

//import collabo.javahl.App;
import io.teamcode.domain.entity.ApplicationSetting;
import io.teamcode.repository.ApplicationSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;

/**
 * TeamCode Application Service
 */
@Service
@Transactional(readOnly = true)
public class ApplicationService {

    @Autowired
    ApplicationSettingRepository applicationSettingRepository;

    @Transactional
    public void updateApplicationSetting(ApplicationSetting applicationSetting) {
        applicationSetting.setUpdatedAt(new Date());
        applicationSettingRepository.save(applicationSetting);
    }

    public ApplicationSetting getApplicationSetting() {
        Iterable<ApplicationSetting> applicationSettings = applicationSettingRepository.findAll();
        Iterator<ApplicationSetting> applicationSettingIterator = applicationSettings.iterator();
        ApplicationSetting applicationSetting = null;
        if (applicationSettingIterator.hasNext()) {
            applicationSetting = applicationSettingIterator.next();
        }

        return applicationSetting;
    }
}
