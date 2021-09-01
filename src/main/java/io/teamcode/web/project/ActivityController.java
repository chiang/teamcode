package io.teamcode.web.project;

import io.teamcode.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 27..
 */
@Controller
@RequestMapping("/{groupPath}/{projectPath}/activity")
public class ActivityController {

    @Autowired
    EventService eventService;

    @GetMapping
    public String list(@PathVariable String projectPath, Model model) {
        model.addAttribute("activities", eventService.getEventsByProject(projectPath));

        return "projects/activity/show";
    }
}
