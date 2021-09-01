package io.teamcode.web;

import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ProjectAttachment;
import io.teamcode.service.AttachmentService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.util.UrlUtils;
import io.teamcode.web.ui.AttachmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by chiang on 2017. 1. 19..
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}/downloads")
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    AttachmentService attachmentService;

    @GetMapping
    public String list(@PathVariable String projectPath, Model model) {
        List<ProjectAttachment> projectAttachments = attachmentService.getProjectAttachments(projectPath);
        model.addAttribute("attachments", projectAttachments);
        model.addAttribute("disabledUploads", tcConfig.getAttachmentMaxFiles() <= projectAttachments.size());

        return "projects/downloads/list";
    }

    //ajax upload handler

    /**
     * id: r5XyQhYkdvOaR4IjtUpV5zQSC
     * file_name: bower.json
     * url: https://chiang.zendesk.com/attachments/token/ZxoZPHk1NTK7HazPpfBA0ekl3/?name=bower.json
     * delete_url: /hc/en-us/request_uploads/r5XyQhYkdvOaR4IjtUpV5zQSC
     *
     *

     * @param multipartFile
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public AttachmentResponse addAttachment(@PathVariable String groupPath,
                                            @PathVariable String projectPath,
                                            @RequestParam(value = "contentType", required = false) String contentType,
                                            @RequestParam("attachment") MultipartFile multipartFile) throws IOException {

        Long attachmentId = attachmentService.storeInProject(projectPath, contentType, multipartFile);

        AttachmentResponse attachmentResponse = new AttachmentResponse();
        attachmentResponse.setId(attachmentId);
        attachmentResponse.setDeleteUrl(String.format("/%s/%s/downloads/%s", groupPath, projectPath, attachmentId));

        return attachmentResponse;
    }

    @GetMapping("/{fileName:.+}")
    public void download(@PathVariable String projectPath, @PathVariable String fileName, HttpServletResponse response) throws IOException {
        logger.debug("request file name: {}", fileName);

        ProjectAttachment projectAttachment = attachmentService.getProjectAttachment(projectPath, fileName, true);
        Path file = Paths.get(tcConfig.getAttachmentsDir().getAbsolutePath(), projectAttachment.getPath());

        if (Files.exists(file)) {
            response.setContentType(projectAttachment.getContentType());
            response.setContentLengthLong(projectAttachment.getSize());
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.addHeader("Content-Disposition", "ticketCommentAttachment; filename*=UTF-8''" + UrlUtils.encodeURIComponent(projectAttachment.getOriginalFileName()));
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
            catch (IOException ex) {
                logger.error("사용자가 다운로드 요청을 하였으나 오류가 발생하여 파일을 전달할 수 없습니다.", ex);
                //ex.printStackTrace();
                //FIXME 이렇게 하면 될까? 사용자가 정확한 이유를 알아야 하나?
                throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
            }
        } else {
            logger.warn("사용자가 다운로드 요청한 파일 '{}' 을 찾을 수 없습니다. 전체 경로: {}", fileName, file.toAbsolutePath());

            throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
        }
    }

    @DeleteMapping(value = "/{attachmentId}")
    //@ResponseStatus(HttpStatus.OK)
    public String delete(@PathVariable String groupPath,
                       @PathVariable String projectPath,
                       @PathVariable Long attachmentId) {

        attachmentService.delete(projectPath, attachmentId);

        return String.format("redirect:/%s/%s/downloads", groupPath, projectPath);
    }

}
