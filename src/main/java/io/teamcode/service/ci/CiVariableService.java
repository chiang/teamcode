package io.teamcode.service.ci;

import io.teamcode.domain.entity.EntityState;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.ci.CiVariable;
import io.teamcode.repository.CiVariableRepository;
import io.teamcode.service.EntityDuplicatedException;
import io.teamcode.service.ProjectService;
import io.teamcode.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 5. 31..
 */
@Service
@Transactional(readOnly = true)
public class CiVariableService {

    private static final Logger logger = LoggerFactory.getLogger(CiVariableService.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    CiVariableRepository ciVariableRepository;

    public List<CiVariable> getCiVariables(final String projectPath) {
        Project project = projectService.getByPath(projectPath);

        return ciVariableRepository.findByProject(project);
    }

    @Transactional
    public CiVariable addCiVariable(final String projectPath, CiVariable ciVariable) {
        Project project = projectService.getByPath(projectPath);

        CiVariable existVar = ciVariableRepository.findByProjectAndName(project, ciVariable.getName());
        if (existVar != null) {
            throw new EntityDuplicatedException(String.format("파이프라인 변수 '%s' 가 이미 등록되어 있습니다.", ciVariable.getName()));
        }

        User currentUser = userService.getCurrentUser();

        ciVariable.setProject(project);
        ciVariable.setEntityState(EntityState.CREATED);
        ciVariable.setCreatedBy(currentUser);
        ciVariable.setUpdatedBy(currentUser);
        ciVariable.setCreatedAt(new Date());
        ciVariable.setUpdatedAt(new Date());

        logger.debug("프로젝트 '{}' 에 새로운 CI 변수 '{}' 을 추가했습니다.", projectPath, ciVariable.getName());

        return ciVariableRepository.save(ciVariable);
    }

    @Transactional
    public void removeCiVariable(final Long ciVariableId) {
        CiVariable ciVariable = ciVariableRepository.findOne(ciVariableId);
        if (ciVariable != null) {
            Project project = ciVariable.getProject();
            this.ciVariableRepository.delete(ciVariable);
            logger.info("프로젝트 '{}' 의 CI 변수 '{}' 을 삭제했습니다.", project.getPath(), ciVariable.getName());
        }
        else {
            logger.warn("CI 변수 '#{}' 를 삭제하는 요청을 받았으나 해당 데이터를 찾을 수 없어 요청을 건너뜁니다.", ciVariableId);
        }
    }

    @Transactional
    public void updateCiVariable(final Long ciVariableId, final CiVariable ciVariable) {
        CiVariable existCiVariable = ciVariableRepository.findOne(ciVariableId);
        existCiVariable.setName(ciVariable.getName());
        existCiVariable.setValue(ciVariable.getValue());

        User currentUser = userService.getCurrentUser();
        ciVariable.setEntityState(EntityState.MODIFIED);
        existCiVariable.setUpdatedBy(currentUser);
        existCiVariable.setUpdatedAt(new Date());

        ciVariableRepository.save(existCiVariable);

        logger.info("프로젝트 '{}' 의 CI 변수 '#{}' 을 업데이트했습니다.", existCiVariable.getProject().getPath(), existCiVariable.getId());
    }
}
