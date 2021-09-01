package io.teamcode.service.project.integration;

import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceCategory;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;

import java.util.List;
import java.util.Map;

/**
 * 프로젝트 별로 통합 (Integration) 하는 기능 중 서비스 를 정의합니다.
 */
public interface ProjectIntegrationService {

    /**
     * 서비스를 구별하기 위한 키. 키 값은 Global 하게 중복될 수 없습니다. 또한 이 키 값은 데이터베이스에서 서비스 정보를 구별하는데도 사용을 합니다.
     * 그래서 데이터베이스에서는 project_id 와 key 를 Unique 하게 설정합니다.
     *
     * @return
     */
    String getKey();

    String getTitle();

    String getDescription();

    String getLogoPath();

    List<String> getPropertyNames();

    ProjectIntegrationServiceCategory getCategory();

    ProjectIntegrationServiceSettings getDefaultSettings();

    void patch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters);

    void execute(String projectPath, ProjectIntegrationServiceSettings projectIntegrationServiceSettings);
}
