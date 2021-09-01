package io.teamcode.web.internal;

import io.teamcode.TeamcodeUnauthorizedException;
import io.teamcode.common.security.web.ProjectSecurity;
import io.teamcode.domain.entity.ProjectRole;
import io.teamcode.domain.entity.User;
import io.teamcode.service.ProjectService;
import io.teamcode.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by chiang on 2017. 1. 31..
 */
public class GroupAndProjectInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GroupAndProjectInterceptor.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables.containsKey("projectPath")) {
            String projectPath = (String) pathVariables.get("projectPath");

            if (!projectService.canAccess(projectPath)) {
                throw new TeamcodeUnauthorizedException();
            }
            else {
                HandlerMethod handlerMethod = (HandlerMethod)handler;
                Method method = handlerMethod.getMethod();
                if (method.isAnnotationPresent(ProjectSecurity.class)) {
                    ProjectSecurity projectSecurity = method.getAnnotation(ProjectSecurity.class);

                    User currentUser = userService.getSessionUser();
                    if (currentUser.isAdmin()) {
                        return true;
                    }
                    else {
                        ProjectRole currentProjectRole = userService.getCurrentUserProjectRole(projectPath);
                        boolean matched = false;
                        for (ProjectRole pr: projectSecurity.value()) {
                            if (pr == currentProjectRole) {
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            throw new TeamcodeUnauthorizedException();
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {//FIXME what happen?
            if (modelAndView.getViewName() != null && modelAndView.getViewName().startsWith("redirect:/")) {
                return;
            }

            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVariables.containsKey("projectPath")) {
                String projectPath = (String) pathVariables.get("projectPath");
                modelAndView.addObject("paramProject", projectService.getByPath(projectPath));
                modelAndView.addObject("paramProjectPath", projectPath);

                User currentUser = userService.getSessionUser();
                if (currentUser.isAdmin()) {
                    modelAndView.addObject("paramm", true);
                }
                else {
                    ProjectRole currentProjectRole = userService.getCurrentUserProjectRole(projectPath);
                    if (currentProjectRole != null && (currentProjectRole == ProjectRole.MASTER || currentProjectRole == ProjectRole.OWNER)) {
                        modelAndView.addObject("paramm", true);
                    }
                    else {
                        modelAndView.addObject("paramm", false);
                    }
                }
            }
            if (pathVariables.containsKey("groupPath")) {
                String groupPath = (String) pathVariables.get("groupPath");
                modelAndView.addObject("paramGroupPath", groupPath);
            } else {
                //Pseudo Group Path 로 projects 를 설정한다.
                modelAndView.addObject("paramGroupPath", "projects");
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
