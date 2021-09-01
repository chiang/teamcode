package io.teamcode.web.project;

import io.teamcode.model.rest.ProjectIntegrationServiceSettingsResponse;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@Controller
@RequestMapping("/{groupPath}/{projectPath}/admin/settings/integrations")
public class ProjectSettingsIntegrationController {

    @Autowired
    ProjectIntegrationServiceManager projectIntegrationServiceManager;

    @GetMapping
    public String list(@PathVariable String groupPath, @PathVariable String projectPath, Model model) {
        model.addAttribute("settingsList", projectIntegrationServiceManager.getAllSettings(projectPath));

        return "projects/settings/integrations/list";
    }

    @GetMapping("/{serviceName}")
    public String showIntegrationForm(@PathVariable String projectPath, @PathVariable String serviceName, Model model) {
        //String serviceKey = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, serviceName);
        model.addAttribute("serviceName", serviceName);

        return "projects/settings/integrations/configure";
    }

    @GetMapping(value ="/{serviceName}.json", produces = {"application/json"})
    @ResponseBody
    public ProjectIntegrationServiceSettingsResponse getSettings(
            @PathVariable String groupPath,
            @PathVariable String projectPath, @PathVariable String serviceName) throws NoSuchMethodException {
        ProjectIntegrationServiceSettingsResponse response = new ProjectIntegrationServiceSettingsResponse();
        response.setSettings(projectIntegrationServiceManager.getSettingsIfNotExistThenDefault(projectPath, serviceName));

        Map<String, String> pathVariables = new HashMap<>();
        pathVariables.put("groupPath", groupPath);
        pathVariables.put("projectPath", projectPath);
        pathVariables.put("serviceName", serviceName);

        response.add(linkTo(ProjectSettingsIntegrationController.class, pathVariables).withRel("list"));

        //Method method = ProjectSettingsIntegrationController.class.getMethod("patchSettings", String.class, String.class, String.class, HttpServletRequest.class);
        //response.add(linkTo(ProjectSettingsIntegrationController.class, method, pathVariables).withRel("patch"));

        return response;
    }

    @PatchMapping("/{serviceName}")
    public String patchSettings(@PathVariable String groupPath, @PathVariable String projectPath, @PathVariable String serviceName, HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        projectIntegrationServiceManager.patchSettings(projectPath, serviceName, parameters);

        return String.format("redirect:/projects/%s/admin/settings/integrations/%s", projectPath, serviceName);
    }
}
