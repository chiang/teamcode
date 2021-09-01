package io.teamcode.web;

import io.teamcode.common.io.ClasspathResourceReader;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.User;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.UserService;
import org.h2.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created by chiang on 2017. 3. 28..
 */
@Controller
@RequestMapping("/avatar")
public class AvatarController {

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @GetMapping("/langs/{avatarFileName:.+}")
    public void renderLangAvatar(@PathVariable String avatarFileName, HttpServletResponse response) throws IOException {
        response.setContentType("image/svg+xml");

        InputStream inputStream = ClasspathResourceReader.getInputStream(String.format("public/assets/images/avatar/%s", avatarFileName));
        if (inputStream != null) {
            IOUtils.copy(inputStream, response.getOutputStream());
        }
        else {
            inputStream = ClasspathResourceReader.getInputStream("public/assets/images/avatar/default.svg");
            IOUtils.copy(inputStream, response.getOutputStream());
        }
    }

    @GetMapping("/projects/{projectPath}/{avatarFileName:.+}")
    @ResponseStatus(HttpStatus.OK)
    public void renderAvatar(@PathVariable String projectPath, HttpServletResponse response) throws IOException {
        Project project = projectService.getByPath(projectPath);

        if (StringUtils.hasText(project.getAvatarPath())) {
            File file = new File(tcConfig.getAttachmentsDir(), project.getAvatarPath());

            if (file.exists()) {
                Files.copy(file.toPath(), response.getOutputStream());
                response.getOutputStream().flush();
            } else {

                throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
            }
        }
    }

    @GetMapping("/users/{userName}/{avatarFileName:.+}")
    @ResponseStatus(HttpStatus.OK)
    public void renderUserAvatar(@PathVariable String userName, HttpServletResponse response) throws IOException {
        try {
            User user = userService.get(userName);

            if (StringUtils.hasText(user.getAvatarPath())) {
                File userAvatarFile = new File(tcConfig.getAttachmentsDir(), user.getAvatarPath());

                if (userAvatarFile.exists()) {
                    Files.copy(userAvatarFile.toPath(), response.getOutputStream());
                    response.getOutputStream().flush();
                } else {
                    throw new ResourceNotFoundException("요청하신 파일을 찾을 수 없습니다.");
                }
            }
            else {
                InputStream inputStream = ClasspathResourceReader.getInputStream("public/assets/images/avatar/unknown.png");
                IOUtils.copy(inputStream, response.getOutputStream());
            }
        } catch (ResourceNotFoundException e) {
            InputStream inputStream = ClasspathResourceReader.getInputStream("public/assets/images/avatar/unknown.png");
            IOUtils.copy(inputStream, response.getOutputStream());
        }
    }
}
