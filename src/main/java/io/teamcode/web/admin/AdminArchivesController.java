package io.teamcode.web.admin;

import io.teamcode.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Controller
@RequestMapping("/admin/archives")
public class AdminArchivesController {

    @Autowired
    ProjectService projectService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("archivedProjects", projectService.getArchived());

        return "admin/archives/list";
    }
}
