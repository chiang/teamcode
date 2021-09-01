package io.teamcode.web;

import io.teamcode.service.InvalidCurrentPasswordException;
import io.teamcode.service.ProfileService;
import io.teamcode.service.UserService;
import io.teamcode.validation.PasswordValidator;
import io.teamcode.web.ui.view.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chiang on 16. 5. 21..
 */
@Controller
public class LoginController {

    @Autowired
    UserService userService;

    @Autowired
    ProfileService profileService;

    @Autowired
    PasswordValidator passwordValidator;

    @RequestMapping("/login")
    public String show() {
        if (userService.getSessionUser() != null) {
            return "redirect:/";
        }
        else {
            return "login";
        }
    }

    @RequestMapping(value = "/update-password", method = RequestMethod.GET)
    public String showUpdatePassword() {

        return "update-password";
    }

    @RequestMapping(value = "/update-password", method = RequestMethod.POST)
    public String updatePassword(ChangePasswordForm changePasswordForm,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest httpServletRequest) {

        passwordValidator.validate(changePasswordForm, result);

        try {
            if(result.hasErrors()){
                //model.addAttribute("validationErrors", result.getFieldErrors());

                //redirectAttributes.addFlashAttribute("alert", result.getFieldErrors().get(0).getDefaultMessage());
                //TODO 좀 그렇다. 매끄러운 방식으로 ...
                /*String firstError = result.getFieldErrors().get(0).getField();
                if (firstError.equals("passwordConfirmation"))
                    redirectAttributes.addFlashAttribute("alert", "재확인 비밀번호가 맞지 않습니다.");
                else if (firstError.equals("passwordTooShort"))
                    redirectAttributes.addFlashAttribute("alert", "비밀번호는 8자 이상으로 입력해 주세요.");*/

                redirectAttributes.addFlashAttribute("alert", result.getFieldErrors().get(0).getCode());
                return "redirect:/update-password";
            }
            else {
                profileService.changePassword(changePasswordForm);

                return "redirect:/login";
            }
        } catch (InvalidCurrentPasswordException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());

            return "redirect:/update-password";
        } catch (Throwable t) {
            redirectAttributes.addFlashAttribute("alert", t.getMessage());

            return "redirect:/update-password";
        }
    }
}
