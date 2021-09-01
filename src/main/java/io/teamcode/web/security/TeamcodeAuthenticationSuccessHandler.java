package io.teamcode.web.security;

import io.teamcode.domain.TcUserDetails;
import io.teamcode.domain.entity.User;
import io.teamcode.service.AccountAuditEventService;
import io.teamcode.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chiang on 16. 5. 22..
 */
@Component
public class TeamcodeAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(TeamcodeAuthenticationSuccessHandler.class);

    @Autowired
    UserService userService;

    @Autowired
    AccountAuditEventService accountAuditEventService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {

        super.clearAuthenticationAttributes(request);

        TcUserDetails tcUserDetails = (TcUserDetails)authentication.getPrincipal();
        User user = userService.get(tcUserDetails.getUsername());
        updateLoginActivity(request, user);
        request.getSession(true).setAttribute("currentUser", user);
        logger.debug("Authentication was loaded successfully with role: {}", tcUserDetails.getAuthorities());

        super.getRedirectStrategy().sendRedirect(request, response, "/");



        /*EsUserDetails userDetails = (EsUserDetails) authentication.getPrincipal();
        logger.debug("on success with user '{}'", userDetails.getUsername());
        User user = userService.getByEmailAddress(userDetails.getUsername());
        logger.debug("current user: {}", user.getEmailAddress());
        request.getSession(true).setAttribute("esUser", user);*/

        /*TerpUserDetails userDetails = (TerpUserDetails)authentication.getPrincipal();
        updateLoginActivity(userDetails.getUsername(), request);

        User user = userManagementService.getUser(userDetails.getUsername());
        Partner partner = userManagementService.getUserOrganization(user);
        user.setBelongTo(partner);
        if (partner != null)
            logger.debug("사용자 '{} 가 속한 파트너 정보는 '{}' 입니다.", user.getUserId(), partner.getName());
        request.getSession(true).setAttribute("terpUser", user);*/

        /*if (user.getRoles().contains(UserRoleType.ROLE_ADMIN.toString()))
            super.getRedirectStrategy().sendRedirect(request, response, "/agent/tickets");
        else
            super.getRedirectStrategy().sendRedirect(request, response, "/hc/tickets?status=OPEN");*/
    }

    private final void updateLoginActivity(HttpServletRequest servletRequest, User user) {
        try {
            accountAuditEventService.createLoginEvent(servletRequest, user);
        } catch (Throwable t) {
            //ignore
            //FIXME 위의 에러 메시지와 분리해서 정리할 필요가 있음
            logger.error("오류가 발생하여 사용자 '{}'의 접속 이력 정보를 저장할 수 없습니다.", user.getName(), t);
        }
    }

}
