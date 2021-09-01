package io.teamcode.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 13..
 */
@Controller
@RequestMapping("/admin/monitoring")
public class AdminMonitoringController {

    @GetMapping("/systeminfo")
    public String system() {

        return "admin/monitoring/systeminfo";
    }

    @GetMapping("/health")
    public String health() {

        return "admin/monitoring/health";
    }

    @GetMapping("/logs")
    public String logs() {

        return "admin/monitoring/logs";
    }

    @GetMapping("/requests")
    public String requests() {

        return "admin/monitoring/requests";
    }
}
