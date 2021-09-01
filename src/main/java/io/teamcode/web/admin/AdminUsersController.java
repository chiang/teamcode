package io.teamcode.web.admin;

import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.UserState;
import io.teamcode.dto.UserListView;
import io.teamcode.service.InvalidCurrentPasswordException;
import io.teamcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 사용자를 관리하는 Controller.
 */
@Controller
@RequestMapping("/admin")
public class AdminUsersController {

    @Autowired
    UserService userService;

    @GetMapping("/users")
    public String list(@RequestParam(required = false) String filter, @SortDefault(value = "fullName", direction = Sort.Direction.ASC) Sort sort, Model model) {
        model.addAttribute("userListView", userService.getUserListView(filter, sort));

        return "admin/users/list";
    }

    //TODO remove sort?
    @GetMapping(value = "/users.json", produces = {"application/json"})
    @ResponseBody
    public UserListView getUserListView(@RequestParam(required = false) String filter, @SortDefault(value = "fullName", direction = Sort.Direction.ASC) Sort sort) {

        return userService.getUserListView(filter, sort);
    }

    @GetMapping("/users/{name}")
    public String show(@PathVariable String name, Model model) {
        model.addAttribute("user", userService.get(name));

        return "admin/users/show";
    }

    @GetMapping(value = "/users/{name}", params = "edit")
    public String editForm(@PathVariable String name, Model model) {
        model.addAttribute("user", userService.get(name));

        return "admin/users/edit";
    }

    //TODO 패스워드 길이 체크
    @PutMapping(value = "/users/{userName}")
    public String edit(@PathVariable String userName,
                       @RequestParam(required = false) String passwordConfirmation,
                       User user, RedirectAttributes redirectAttributes) {

        try {
            userService.update(userName, user, passwordConfirmation);
            redirectAttributes.addFlashAttribute("notice", "User was updated successfully.");
        } catch (InvalidCurrentPasswordException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());

            return String.format("redirect:/admin/users/%s?edit", user.getName());
        }

        return String.format("redirect:/admin/users/%s", user.getName());
    }

    @GetMapping(value = "/users", params = {"create"})
    public String createForm() {

        return "admin/users/createForm";
    }

    @PostMapping("/users")
    public String create(User user, RedirectAttributes redirectAttributes) {
        user.setState(UserState.ACTIVE);//TODO consider policy
        userService.newUser(user);
        redirectAttributes.addFlashAttribute("notice", "User was created successfully.");

        return String.format("redirect:/admin/users/%s", user.getName());
    }

    @PutMapping(value = "/users/{userName}/block")
    public String block(@PathVariable String userName) {

        userService.block(userName);

        return String.format("redirect:/admin/users/%s", userName);
    }

    @PutMapping(value = "/users/{userName}/unblock")
    public String unblock(@PathVariable String userName) {

        userService.unblock(userName);

        return String.format("redirect:/admin/users/%s", userName);
    }

}
