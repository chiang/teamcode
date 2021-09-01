package io.teamcode.web.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by chiang on 2017. 2. 5..
 */
@Controller
@RequestMapping("/admin/messages")
public class AdminMessagesController {

    @GetMapping
    public String show() {

        return "admin/messages/show";
    }

}
