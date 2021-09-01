package io.teamcode.web.admin;

import io.teamcode.service.ci.RunnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 4. 23..
 */
@Controller
@RequestMapping("/admin/runners")
public class AdminRunnersController {

    @Autowired
    RunnerService runnerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("runnersView", runnerService.getRunnersView());

        return "admin/runners/list";
    }

    @PutMapping(params = "resetToken")
    public String reset() {
        runnerService.resetRunnerToken();

        return "redirect:/admin/runners";
    }

}
