package io.teamcode.service;

import io.teamcode.TeamcodeException;
import io.teamcode.common.Strings;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectAttachment;
import io.teamcode.domain.entity.User;
import io.teamcode.repository.ProjectAttachmentRepository;
import io.teamcode.util.StorageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by chiang on 2017. 2. 1..
 */
@Service
@Transactional(readOnly = true)
public class AttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    ProjectAttachmentRepository projectAttachmentRepository;

    public long countAll() {

        return projectAttachmentRepository.count();
    }

    public List<ProjectAttachment> getProjectAttachments(final String projectPath) {
        Project project = projectService.getByPath(projectPath);

        return projectAttachmentRepository.findByProject(project);
    }

    /**
     *
     *
     * @param projectPath
     * @param fileName
     * @return
     */
    @Transactional
    public ProjectAttachment getProjectAttachment(final String projectPath, final String fileName, boolean isDownloadRequest) {
        Project project = projectService.getByPath(projectPath);
        ProjectAttachment projectAttachment = projectAttachmentRepository.findByProjectAndOriginalFileName(project, fileName);

        if (projectAttachment != null) {

            if (isDownloadRequest) {
                projectAttachment.plusDownloads();
                projectAttachmentRepository.save(projectAttachment);
            }

            return projectAttachment;
        }

        throw new ResourceNotFoundException("Sorry. The file you are looking for does not exist");
    }

    /**
     * ???????????? ??????????????? ???????????? ????????? ???????????????. ?????? ????????? ????????? ?????? ???????????? ?????? ?????? ????????? ????????? ?????? ?????????, ??????????????? ??????
     * ????????? ????????? ???????????????. ?????? ??????, ????????? ???????????? ??????, ????????? ??? ??????????????? ????????? ????????? ????????? ????????? ??? ????????????. ????????? ????????? ?????????
     * ??????????????? ?????? ???????????????.
     *
     * ?????? [brand mod 3]/[brand mod 4]/[brand-id]/_staged_/[first char]/[second char]/file-name ????????? ?????? ????????? ???????????????
     * ???????????? ?????? ?????? ??????????????? ???????????????.
     *
     * TODO ??? ????????? ???????????? ?????? ?????? ????????? ????????? ??????.
     *
     *
     * @param projectPath
     * @param contentType ??????????????? ????????? ????????? contentType
     * @param multipartFile
     * @return
     */
    @Transactional
    public Long storeInProject(final String projectPath, final String contentType, final MultipartFile multipartFile) throws IOException {
        logger.debug("Project attachments root directory: {}", tcConfig.getAttachmentsDir().getAbsolutePath());

        User currentUser = userService.getCurrentUser();
        Project project = projectService.getByPath(projectPath);
        String normalizedOriginalFileName = Strings.normalizeToNfc(multipartFile.getOriginalFilename());
        ProjectAttachment projectAttachment = projectAttachmentRepository.findByProjectAndOriginalFileName(project, normalizedOriginalFileName);

        try {
            if (projectAttachment == null) {
                projectAttachment = new ProjectAttachment();
                //TODO UUID ??? ????????? ????????? ?????? ???????????? ?????? ???????????? ?????? ?????? ????????? ????????? ????????? ?????? (?????? ????????? ?????????????)
                projectAttachment.setFileName(UUID.randomUUID().toString());

                if (StringUtils.hasText(contentType)) {
                    projectAttachment.setContentType(contentType);
                }
                else {
                    MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
                    projectAttachment.setContentType(mimetypesFileTypeMap.getContentType(normalizedOriginalFileName));
                }
                logger.debug("project attachment file content-type: {}", projectAttachment.getContentType());

                String extension = FilenameUtils.getExtension(normalizedOriginalFileName);
                if (StringUtils.hasText(extension))
                    projectAttachment.setExtension(extension);
            }

            projectAttachment.setOriginalFileName(normalizedOriginalFileName);
            projectAttachment.setSize(multipartFile.getSize());

            String directoryPath = StorageUtils.buildStagedHierarchyStructure(project.getId(), projectAttachment.getFileName());
            File directory = new File(tcConfig.getAttachmentsDir(), directoryPath);
            //TODO synchronization?
            if(!directory.exists()) {
                if (!directory.mkdirs())
                    throw new TeamcodeException(String.format("??????????????? ?????? ??? ????????????! %s", directory.getAbsolutePath()));
            }
            File statedFile;
            if (StringUtils.hasText(projectAttachment.getExtension()))
                statedFile = new File(directory, String.format("%s.%s", projectAttachment.getFileName(), projectAttachment.getExtension()));
            else
                statedFile = new File(directory, projectAttachment.getFileName());

            //TODO ?????? ?????? ???????
            if (statedFile.exists()) {
                throw new IllegalArgumentException("????????? ?????????????????????.");
            }

            StringBuilder path = new StringBuilder();
            path.append(directoryPath).append(File.separator).append(projectAttachment.getFileName());
            if (StringUtils.hasText(projectAttachment.getExtension()))
                path.append(".").append(projectAttachment.getExtension());
            projectAttachment.setPath(path.toString());

            projectAttachment.setAuthor(currentUser);
            projectAttachment.setCreatedAt(new Date());
            projectAttachment = projectAttachmentRepository.save(projectAttachment);
            multipartFile.transferTo(statedFile);

            logger.debug("Transferring the staged file to production...");
            String productionDirPath = StorageUtils.buildProjectAttachmentsHierarchyStructure(project.getId(), projectAttachment.getFileName());
            File productionDir = new File(tcConfig.getAttachmentsDir(), productionDirPath);
            //TODO synchronization?
            if(!productionDir.exists()) {
                if (!productionDir.mkdirs())
                    throw new TeamcodeException(String.format("??????????????? ?????? ??? ????????????! %s", productionDir.getAbsolutePath()));
            }
            File productionFile = new File(productionDir, statedFile.getName());
            FileUtils.moveFile(statedFile, productionFile);

            //project Entity ??? ???????????? stage ???????????? production ????????? ???????????? ???????????????.
            projectAttachment.setProject(project);
            StringBuilder productionFilePath = new StringBuilder();
            productionFilePath.append(productionDirPath).append(File.separator).append(projectAttachment.getFileName());
            if (StringUtils.hasText(projectAttachment.getExtension()))
                productionFilePath.append(".").append(projectAttachment.getExtension());

            projectAttachment.setPath(productionFilePath.toString());
            projectAttachment = projectAttachmentRepository.save(projectAttachment);

            logger.info("???????????? ???????????? ?????? '{}' ??? ??????????????????.", projectAttachment.getOriginalFileName());

            return projectAttachment.getId();
        } catch (IOException e) {
            //throw new TicketCreationException("Cannot save ticket comment attachment file.", e);
            throw e;
        }
    }

    //
    public Long storeAvatar(final String userName, final MultipartFile multipartFile) throws IOException {
        //TODO ext
        File avatarFile = new File(tcConfig.getUsersAttachmentsDir(), userName);
        multipartFile.transferTo(avatarFile);
        logger.debug("User '{}' avatar was saved to '{}'.", userName, avatarFile.getAbsolutePath());

        return 0l;
    }

    @Transactional
    public void delete(final String projectPath, final Long attachmentId) {
        logger.debug("Deleting a project attachment file and data... id: {}", attachmentId);

        User currentUser = userService.getCurrentUser();
        //TODO blocking and project member
        //if (currentUser.)

        ProjectAttachment projectAttachment = projectAttachmentRepository.findOne(attachmentId);
        if (projectAttachment != null) {
            projectAttachmentRepository.delete(attachmentId);

            File fileToDelete = new File(tcConfig.getAttachmentsDir(), projectAttachment.getPath());
            if(!fileToDelete.delete()) {
                //TODO custom exception
                throw new RuntimeException(String.format("Cannot delete a file '%s'.", fileToDelete.getName()));
            }

            logger.debug("The project attachment entity and file was deleted.");
        }
    }
}
