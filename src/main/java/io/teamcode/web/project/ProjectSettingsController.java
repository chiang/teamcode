package io.teamcode.web.project;

import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.security.web.ProjectSecurity;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.*;
import io.teamcode.domain.entity.ci.CiVariable;
import io.teamcode.service.IssueLabelService;
import io.teamcode.service.MilestoneService;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ProjectSettingsService;
import io.teamcode.service.ci.CiVariableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 27..
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}/admin")
public class ProjectSettingsController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSettingsController.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectSettingsService projectSettingsService;

    @Autowired
    IssueLabelService issueLabelService;

    @Autowired
    MilestoneService milestoneService;

    @Autowired
    CiVariableService ciVariableService;

    @GetMapping(params = "edit")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String editForm(@PathVariable String projectPath, Model model) {

        model.addAttribute("externalUrl", tcConfig.getExternalUrl());
        model.addAttribute("project", projectService.getByPath(projectPath));
        model.addAttribute("populars", ProgrammingLanguage.populars());
        model.addAttribute("others", ProgrammingLanguage.others());

        return "projects/settings/edit";
    }

    @PatchMapping
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String patch(@PathVariable String projectPath,
                        Project project,
                        @RequestParam(value = "avatar", required = false) MultipartFile multipartFile,
                        RedirectAttributes redirectAttributes) {
        try {
            projectService.patch(projectPath, project, multipartFile);
            redirectAttributes.addFlashAttribute("notice", "Project was updated successfully.");
        } catch (MaxUploadSizeExceededException e) {
            redirectAttributes.addFlashAttribute("alertType", "MAX_SIZE");
            redirectAttributes.addFlashAttribute("alert", TeamcodeConstants.MAX_UPLOAD_AVATAR_ERROR_MESSAGE);
        }

        return String.format("redirect:/projects/%s/admin?edit", projectPath);
    }

    @GetMapping("/access")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String members(@PathVariable String projectPath, Model model) {
        model.addAttribute("members", projectService.getMembers(projectPath));

        return "projects/settings/members";
    }

    @GetMapping("/path")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String path(@PathVariable String projectPath, Model model) {
        model.addAttribute("externalUrl", tcConfig.getExternalUrl());
        model.addAttribute("project", projectService.getByPath(projectPath));

        return "projects/settings/path";
    }

    @PatchMapping("/path")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String patchPath(@PathVariable String groupPath, @PathVariable String projectPath, Project project) {
        Project patched = projectService.patch(projectPath, project, null);
        logger.debug("프로젝트 #{} 의 이름 및 경로 정보를 이름 -> '{}', 경로 -> '{}' 로 패치했습니다.", patched.getId(), patched.getName(), patched.getPath());

        return String.format("redirect:/%s/%s/admin/path", groupPath, patched.getPath());
    }

    @GetMapping("/archive")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String archive(@PathVariable String projectPath, Model model) {
        model.addAttribute("project", projectService.getByPath(projectPath));

        return "projects/settings/archive";
    }

    //아직 멤버가 아닌 사용자 목록을 조회합니다...
    @GetMapping(value="/members", params = {"not"}, produces={"application/xml", "application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<User> getNotMembers(@PathVariable String projectPath, @RequestParam(required = false) String query) {

        return projectService.getNotMembers(projectPath, query);
    }

    @GetMapping("/pipeline/settings")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String pipelineSettings(@PathVariable String projectPath, Model model) {
        model.addAttribute("project", projectService.getByPath(projectPath));

        return "projects/settings/pipelines/show";
    }

    @PatchMapping("/pipeline/settings")
    @ResponseStatus(HttpStatus.OK)
    public void patchPipelineSettings(@PathVariable String projectPath, boolean enabled) {

        projectSettingsService.togglePipeline(projectPath, enabled);
        //TODO
        //redirectAttributes.addFlashAttribute("notice", "Project was updated successfully.");

        //return String.format("redirect:/projects/%s/admin/pipelines/settings", projectPath);
    }

    @GetMapping("/pipeline/settings/variables")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String variables(@PathVariable String projectPath, Model model) {

        return "projects/settings/pipelines/variables";
    }

    @GetMapping(value = "/pipeline/settings/variables.json", produces={"application/xml", "application/json"})
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    @ResponseBody
    public List<CiVariable> getVariables(@PathVariable String projectPath) {

        return ciVariableService.getCiVariables(projectPath);
    }

    @PostMapping("/pipeline/settings/variables")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    @ResponseBody
    public CiVariable addVariable(@PathVariable String projectPath, CiVariable ciVariable) {

        return ciVariableService.addCiVariable(projectPath, ciVariable);
    }

    @PostMapping("/pipeline/settings/variables/{ciVariableId}")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    @ResponseStatus(HttpStatus.OK)
    public void updateVariable(@PathVariable Long ciVariableId, CiVariable ciVariable) {

        ciVariableService.updateCiVariable(ciVariableId, ciVariable);
    }

    @DeleteMapping("/pipeline/settings/variables/{ciVariableId}")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    @ResponseStatus(HttpStatus.OK)
    public void removeVariable(@PathVariable Long ciVariableId) {

        ciVariableService.removeCiVariable(ciVariableId);
    }

    @PatchMapping("/pipeline/settings/config-path")
    public String patchPipelineConfigPath(@PathVariable String projectPath, String pipelineConfigPath) {
        Project project = new Project();
        project.setPipelineConfigPath(pipelineConfigPath);

        projectService.patch(projectPath, project, null);

        logger.debug("프로젝트 '{}' 의 파이프라인 설정 파일 경로를 '{}' 로 업데이트했습니다.",  projectPath, pipelineConfigPath);

        return String.format("redirect:/projects/%s/admin/pipeline/settings", projectPath);
    }

    @GetMapping("/issues/labels")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String issueLabels(@PathVariable String projectPath,  Model model) {
        model.addAttribute("labels", issueLabelService.list(projectPath));

        return "projects/settings/issues/labels/list";
    }

    @GetMapping(value = "/issues/labels.json", produces={"application/xml", "application/json"})
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    @ResponseBody
    public List<IssueLabel> getIssueLabels(@PathVariable String projectPath) {

        return issueLabelService.list(projectPath);
    }


    @GetMapping(value = "/issues/labels", params = "createDefault")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String createDefaultIssueLabels(@PathVariable String projectPath) {
        issueLabelService.createDefaultLabels(projectPath);

        return String.format("redirect:/projects/%s/admin/issues/labels", projectPath);
    }

    @GetMapping(value = "/issues/labels", params = "create")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String createIssueLabelForm() {

        return "projects/settings/issues/labels/createForm";
    }

    @PostMapping(value = "/issues/labels")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String createIssueLabel(@PathVariable String projectPath, IssueLabel issueLabel) {
        issueLabelService.create(projectPath, issueLabel);

        return String.format("redirect:/projects/%s/admin/issues/labels", projectPath);
    }

    @GetMapping(value = "/issues/labels/{labelId}")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String editLabels(@PathVariable String projectPath, @PathVariable Long labelId) {
        //issueLabelService.createDefaultLabels(projectPath);

        return String.format("redirect:/projects/%s/admin/issues/labels", projectPath);
    }

    @GetMapping("/milestones")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String milestones(@PathVariable String projectPath, @RequestParam(required = false, defaultValue = "all") String state, Model model) {
        model.addAttribute("milestonesView", milestoneService.list(projectPath, state));

        return "projects/settings/milestones/list";
    }

    @GetMapping(value = "/milestones", params = "create")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String milestonesCreateForm(@PathVariable String projectPath, Model model) {
        model.addAttribute("endpoint", String.format("/projects/%s/admin/milestones", projectPath));

        return "projects/settings/milestones/createForm";
    }

    @GetMapping("/milestones/{milestoneId}")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String showMilestone(@PathVariable String projectPath, @PathVariable Long milestoneId, Model model) {
        model.addAttribute("milestone", milestoneService.get(milestoneId));

        return "/projects/settings/milestones/show";
    }

    @PostMapping(value = "/milestones")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String addMilestone(@PathVariable String projectPath, Milestone milestone) {
        milestoneService.create(projectPath, milestone);

        return String.format("redirect:/projects/%s/admin/milestones", projectPath);
    }


    @PostMapping("/members")
    @ProjectSecurity({ProjectRole.MASTER, ProjectRole.OWNER})
    public String addUsers(@PathVariable String groupPath,
                           @PathVariable String projectPath,
                           @RequestParam ProjectRole projectRole,
                           @RequestParam List<Long> userIds,
                           RedirectAttributes redirectAttributes) {

        projectService.addUsers(projectPath, userIds, projectRole);
        redirectAttributes.addFlashAttribute("notice", "Users were successfully added.");

        return String.format("redirect:/%s/%s/admin/access", groupPath, projectPath);
    }

    @PatchMapping("/members/{memberId}")
    public String patchMember(@PathVariable String groupPath,
                              @PathVariable String projectPath,
                              @PathVariable Long memberId, ProjectMember projectMember) {

        projectService.patchMember(projectPath, memberId, projectMember);
        return String.format("redirect:/%s/%s/admin/access", groupPath, projectPath);
    }

    @DeleteMapping("/members/{memberId}")
    public String removeMember(@PathVariable String groupPath,
                           @PathVariable String projectPath,
                           @PathVariable Long memberId,
                           RedirectAttributes redirectAttributes) {

        projectService.removeMember(projectPath, memberId);
        redirectAttributes.addFlashAttribute("notice", "Users were successfully removed.");

        return String.format("redirect:/%s/%s/admin/access", groupPath, projectPath);
    }

    @PostMapping("/links")
    @ResponseStatus(HttpStatus.OK)
    public void addLink(@PathVariable String groupPath, @PathVariable String projectPath, String title, String link) {

        projectService.addLink(projectPath, title, link);
    }

    @DeleteMapping("/links/{linkId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLink(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable Long linkId) {

        projectService.deleteLink(linkId);
    }
}
