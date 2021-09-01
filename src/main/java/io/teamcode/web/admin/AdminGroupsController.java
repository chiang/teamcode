package io.teamcode.web.admin;

import io.teamcode.domain.entity.Group;
import io.teamcode.domain.entity.GroupType;
import io.teamcode.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO 아직 구현하지 않음....
 */
@Controller
@RequestMapping("/admin/groups")
public class AdminGroupsController {

    @Autowired
    GroupService groupService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("groups", groupService.getGroups());

        return "admin/groups/list";
    }

    @GetMapping("/{groupPath}")
    public String show(@PathVariable String groupPath, Model model) {
        model.addAttribute("group", groupService.getByPath(groupPath));

        return "admin/groups/show";
    }

    @GetMapping(params = {"create"})
    public String createForm() {

        return "admin/groups/createForm";
    }

    @PostMapping
    public String create(Group group) {
        group.setType(GroupType.CUSTOM);
        groupService.create(group);
        //Group was created successfully.

        return String.format("redirect:/admin/groups/%s", group.getPath());
    }

}
