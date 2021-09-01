package io.teamcode.web;

import io.teamcode.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 6..
 */
@Controller
@RequestMapping(value = "/{groupPath}")
public class GroupedProjectController {

    @Autowired
    GroupService groupService;

    @GetMapping
    public String index(@PathVariable String groupPath, Model model) {
        //model.addAttribute("group", groupService.getByPath(groupPath));

        return "group/index";
    }

    @GetMapping("/{projectName}")
    public String show(@PathVariable String groupPath, @PathVariable String projectName) {

        return String.format("forward:/projects/%s", projectName);
    }

    @GetMapping(params = {"create"})
    public String createForm(@PathVariable String groupPath) {
        System.out.println("------->> groupPath: " + groupPath);

        return "project/createForm";
    }




}
