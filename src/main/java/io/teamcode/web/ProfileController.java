package io.teamcode.web;

import io.teamcode.common.TeamcodeConstants;
import io.teamcode.domain.Theme;
import io.teamcode.domain.entity.User;
import io.teamcode.service.EmailAlreadyExistException;
import io.teamcode.service.InvalidCurrentPasswordException;
import io.teamcode.service.ProfileService;
import io.teamcode.util.EmailUtils;
import io.teamcode.validation.PasswordValidator;
import io.teamcode.web.ui.view.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by chiang on 2017. 2. 1..
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    ProfileService profileService;

    @Autowired
    PasswordValidator passwordValidator;

    @GetMapping
    public String profile(Model model) {
        model.addAttribute("profile", profileService.get());

        return "profile/profile";
    }

    @PutMapping
    public String update(User user,
                         @RequestParam(value = "avatar", required = false) MultipartFile multipartFile,
                         RedirectAttributes redirectAttributes) {
        try {
            profileService.update(user, multipartFile);
        } catch (MaxUploadSizeExceededException e) {
            redirectAttributes.addFlashAttribute("alertType", "MAX_SIZE");
            redirectAttributes.addFlashAttribute("alert", TeamcodeConstants.MAX_UPLOAD_AVATAR_ERROR_MESSAGE);
        }

        return "redirect:/profile";
    }

    @GetMapping("/password")
    public String showPasswordForm() {

        return "profile/password";
    }

    @PutMapping(value = "/password", params = {"currentPassword", "password", "passwordConfirmation"})
    public String changePassword(ChangePasswordForm changePasswordForm,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest httpServletRequest) throws ServletException {

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
            }
            else {
                profileService.changePassword(changePasswordForm);

                //redirectAttributes.addFlashAttribute("notice", "Profile was successfully updated");
                //redirectAttributes.addFlashAttribute("notice", "비밀번호가 변경되었습니다.");

                //FIXME 응? 맞나?
                httpServletRequest.logout();
                new SecurityContextLogoutHandler().logout(httpServletRequest, null, null);

                return "redirect:/";
            }
        } catch (InvalidCurrentPasswordException e) {
            redirectAttributes.addFlashAttribute("alert", e.getMessage());
        }

        return "redirect:/profile/password";
    }

    @GetMapping("/emails")
    public String showEmails(Model model) {
        model.addAttribute("userEmails", profileService.getEmails());

        return "profile/emails";
    }

    @PostMapping("/emails")
    public String addEmail(String email, RedirectAttributes redirectAttributes) {
        if (!EmailUtils.validate(email)) {
            redirectAttributes.addFlashAttribute("alert", "Email is invalid.");//TODO i18n
        }
        else {
            try {
                profileService.addEmail(email);
            } catch (EmailAlreadyExistException e) {
                redirectAttributes.addFlashAttribute("alert", e.getMessage());
            }
        }

        return "redirect:/profile/emails";
    }

    @DeleteMapping("/emails/{emailId}")
    public String removeEmail(@PathVariable Long emailId) {
        profileService.removeEmail(emailId);

        return "redirect:/profile/emails";
    }

    @GetMapping("/notifications")
    public String showNotificationsForm(Model model) {
        model.addAttribute("notificationEmails", profileService.getNotificationEmails());

        return "profile/notifications";
    }

    @PutMapping(value = "/notifications", params = {"notificationEmail"})
    public String updateNotificationEmail(User user, RedirectAttributes redirectAttributes) {
        profileService.patch(user);
        //redirectAttributes.addFlashAttribute("notice", "Notification settings saved.");
        redirectAttributes.addFlashAttribute("notice", "알림 설정이 업데이트되었습니다.");

        return "redirect:/profile/notifications";
    }

    @GetMapping("/preferences")
    public String showPreferences(Model model) {
        model.addAttribute("themes", Theme.values());

        return "profile/preferences";
    }

    @PutMapping("/preferences")
    public String patchPreferences(User user, RedirectAttributes redirectAttributes) {
        profileService.patch(user);
        redirectAttributes.addFlashAttribute("notice", "Preferences saved.");

        return "redirect:/profile/preferences";
    }

    @PostMapping(params = {"avatar"})
    @ResponseStatus(HttpStatus.OK)
    public void updateAvatar() {

    }

    @GetMapping("/audit")
    public String showAuditLogs(Model model) {

        return "profile/audit";
    }
}
