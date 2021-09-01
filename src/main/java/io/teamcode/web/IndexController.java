package io.teamcode.web;

import io.teamcode.domain.entity.Project;
import io.teamcode.service.ProjectService;
import io.teamcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 사용자가 로그인한 뒤 처음 나오는 화면입니다.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    @RequestMapping
    public String index() {

        return "redirect:/dashboard";
    }


    @RequestMapping("/dashboard")
    public String dashboard(@RequestParam(name = "name", required = false) String searchWord,
                            @SortDefault(value = "name", direction = Sort.Direction.ASC) Sort sort,
                            Model model) {

        //TODO index.html 제거하기 (아래 user-index.html 을 사용하니까)
        if (userService.getCurrentUser().isAdmin()) {
            model.addAttribute("sortOrder", sort.iterator().next());
            model.addAttribute("projects", projectService.getAllProjects(searchWord, sort));

            return "user-index";
        }
        else {
            model.addAttribute("sortOrder", sort.iterator().next());
            model.addAttribute("projects", projectService.getMyProjects());

            return "user-index";
        }
    }

    @RequestMapping("/projects")
    public String listProjects(@RequestParam(name = "name", required = false) String searchWord,
                               @SortDefault(value = "name", direction = Sort.Direction.ASC) Sort sort, Model model) {

        return "projects";
    }

    @GetMapping(value= "/projects.json", produces = {"application/json"})
    @ResponseBody
    public List<Project> getMyProjects() {

        return projectService.getMyProjects();
    }

}
