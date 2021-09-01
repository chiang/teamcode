package io.teamcode.web.admin;

import io.teamcode.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 5..
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    @GetMapping
    public String overview(Model model) {
        model.addAttribute("adminOverviewView", adminService.getAdminOverviewView());

        return "admin/overview/overview";
    }

}
