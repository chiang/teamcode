package io.teamcode.web.admin;

import io.teamcode.TeamcodeException;
import io.teamcode.domain.entity.ProgrammingLanguage;
import io.teamcode.domain.entity.Project;
import io.teamcode.service.AlreadyCreatedException;
import io.teamcode.service.ProjectService;
import io.teamcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chiang on 2017. 2. 12..
 */
@Controller
@RequestMapping("/admin/projects")
public class AdminProjectsController {

    @Autowired
    ProjectService projectService;


    @GetMapping
    public String list(@RequestParam(name = "name", required = false) String searchWord,
                       @SortDefault(value = "name", direction = Sort.Direction.ASC) Sort sort,
                       Model model) {

        if(StringUtils.hasText(searchWord))
            model.addAttribute("searchWord", searchWord);
        model.addAttribute("sortOrder", sort.iterator().next());
        model.addAttribute("projects", projectService.getAllProjects(searchWord, sort));

        return "admin/projects/list";
    }

    @GetMapping(params = {"create"})
    public String createForm(Model model) {
        model.addAttribute("populars", ProgrammingLanguage.populars());
        model.addAttribute("others", ProgrammingLanguage.others());

        return "admin/projects/createForm";
    }

    //TODO group 은 나중에 처리하기로 합세.
    @PostMapping
    public String create(@RequestParam(required = false) String groupPath, Project project, RedirectAttributes redirectAttributes) {
        try {
            projectService.create(groupPath, project);
            redirectAttributes.addFlashAttribute("notice", String.format("Project '%s' was successfully created.", project.getName()));
        } catch (AlreadyCreatedException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());
        }

        return String.format("redirect:/projects/%s?edit", project.getPath());
    }

    /*@GetMapping(value = "/{projectPath}", params = "edit")
    public String editForm(@PathVariable String projectPath, Model model) {
        model.addAttribute("project", projectService.getByPath(projectPath));

        return "admin/projects/edit";
    }

    @PatchMapping(value = "/{name}")
    public String edit(@PathVariable String name, Project project, RedirectAttributes redirectAttributes) {
        projectService.patch(name, project);
        redirectAttributes.addFlashAttribute("notice", "Project was updated successfully.");

        return String.format("redirect:/admin/projects/%s?edit", project.getName());
    }*/

    @DeleteMapping(value = "/{path}")
    public String delete(@PathVariable String path, RedirectAttributes redirectAttributes) {
        projectService.delete(path);

        return "redirect:/admin/projects";
    }

    @PutMapping(value = "/{path}/archive")
    public String archive(@PathVariable String path, RedirectAttributes redirectAttributes) {
        try {
            projectService.archive(path);
        } catch (TeamcodeException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());

            //TODO 오류 처리하기
            return String.format("redirect:/projects/%s?edit", path);
        }

        return "redirect:/admin/projects";
    }
}
