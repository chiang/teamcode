package io.teamcode.service;

import io.teamcode.TeamcodeException;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.repository.ProjectRepository;
import io.teamcode.util.StorageUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chiang on 2017. 3. 28..
 */
@Service
@Transactional(readOnly = true)
public class AvatarService {

    private static final Logger logger = LoggerFactory.getLogger(AvatarService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectRepository projectRepository;

    //for internal
    @Transactional
    public void storeAvatar(final long projectId) {
        Project project = projectRepository.findOne(projectId);

        InputStream in = AvatarService.class.getResourceAsStream("/public/assets/images/sample-project.png");

        String projectDirPath = StorageUtils.buildProjectDirHierarchyStructure(projectId);
        File projectDir = new File(tcConfig.getAttachmentsDir(), projectDirPath);

        if (!projectDir.exists()) {
            if (!projectDir.mkdirs())
                throw new TeamcodeException(String.format("디렉터리를 만들 수 없습니다! %s", projectDir.getAbsolutePath()));
        }

        File avatarFile = new File(projectDir, "avatar.png");
        try {
            project.setAvatarPath(String.format("%s/%s", projectDirPath, avatarFile.getName()));
            projectRepository.save(project);

            FileUtils.copyInputStreamToFile(in, avatarFile);
        } catch (IOException e) {
            logger.error("프로젝트 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);

            throw new TeamcodeException("프로젝트 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);
        }
    }
}
