package io.teamcode.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 27..
 */
@Controller
@RequestMapping("/help")
public class HelpController {

    @RequestMapping
    public String list() {

        return "help/list";
    }

    @RequestMapping("/checkout")
    public String checkout() {

        return "help/checkout";
    }

    @RequestMapping("/user/permissions")
    public String permissions() {

        return "help/user/permissions";
    }

    @GetMapping("/ci/quick-start")
    public String ciQuickStart() {

        return "help/ci/quick-start";
    }

    @GetMapping("/ci/variables")
    public String ciVariables() {

        return "help/ci/variables";
    }

    @GetMapping("/admin/system/settings")
    public String adminSystemSettings() {

        return "help/admin/system/settings";
    }

    @GetMapping("/faq")
    public String faq() {

        return "help/faq";
    }
}
