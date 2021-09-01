package io.teamcode.web;

import io.teamcode.TeamcodeUnauthorizedException;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.converter.MarkdownConverter;
import io.teamcode.common.converter.ReadmeConversionException;
import io.teamcode.common.security.web.ProjectSecurity;
import io.teamcode.common.vcs.svn.SvnCommandHelper;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectRole;
import io.teamcode.domain.entity.User;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.UserService;
import io.teamcode.service.vcs.svn.CommitService;
import org.apache.subversion.javahl.types.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by chiang on 2017. 2. 6..
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    CommitService commitService;

    /*@GetMapping(params = {"create"})
    public String createForm(@PathVariable String groupPath) {
        System.out.println("------->> groupPath: " + groupPath);

        return "projects/createForm";
    }

    @PostMapping
    public String create(Project project, RedirectAttributes redirectAttributes) {
        projectService.create(project);
        redirectAttributes.addFlashAttribute("notice", String.format("Project '%s' was successfully created.", project.getName()));

        return String.format("redirect:/projects/%s", project.getName());
    }*/

    @GetMapping("/{projectPath}")
    public String show(@PathVariable String projectPath, Model model) {
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "");
        long headRevisionNumber = SvnCommandHelper.getHeadRevisionNumber(repositoryUri);

        model.addAttribute("headRevisionNumber", headRevisionNumber);
        model.addAttribute("emptyRevision", (headRevisionNumber < 1));
        if (headRevisionNumber > 0) {
            model.addAttribute("lastCommit", commitService.getCommit(projectPath, Revision.HEAD));

            try {
                String readmeFileUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "README.md");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                SvnCommandHelper.getContent(readmeFileUri, outputStream);

                model.addAttribute("readmeContent", MarkdownConverter.convert(new String(outputStream.toByteArray(), "UTF-8")));
            } catch (ReadmeConversionException e) {
                //TODO e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                logger.warn("시스템이 지원하지 않은 인코딩 'UTF-8' 을 지원하지 않아 README 파일을 읽을 수 없습니다.", e);
            } catch (ResourceNotFoundException e) {
                logger.debug("README 파일을 찾을 수 없어 화면에 표시하지 않습니다.");
            } catch (Throwable t) {
                logger.error("오류가 발생하여 README 파일을 읽을 수 없습니다.", t);
            }
        }
        model.addAttribute("project", projectService.getByPath(projectPath));
        model.addAttribute("overview", projectService.getProjectOverviewView(projectPath));//FIXME one transaction?

        return "projects/show";
    }

    @GetMapping("/{projectPath}/members")
    public String listMembers(@PathVariable String projectPath, Model model) {
        model.addAttribute("members", projectService.getMembers(projectPath));

        return "projects/members/list";
    }

}
