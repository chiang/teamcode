package io.teamcode.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FlashAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlashAuthenticationFailureHandler.class);

    /**
     * Flash attribute name to save on redirect.
     */
    public static final String AUTHENTICATION_MESSAGE = "FLASH_AUTHENTICATION_MESSAGE";

    public FlashAuthenticationFailureHandler()
    {
        return;
    }

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) throws IOException, ServletException {
        if (exception != null) {
            logger.debug("사용자 인증 요청 처리 중 오류가 발생했습니다.", exception);
            final FlashMap flashMap = new FlashMap();
            // Don't send the AuthenticationException object itself because it has no default constructor and cannot be re-instantiated.
            flashMap.put(AUTHENTICATION_MESSAGE, exception.getMessage());
            final FlashMapManager flashMapManager = new SessionFlashMapManager();
            flashMapManager.saveOutputFlashMap(flashMap, request, response);

            if (exception instanceof DisabledException) {
                request.getSession(true).setAttribute("USERNAME", ((DisabledException)exception).getUserId());//FIXME 안 되!!
                //flashMap.put("USERNAME", ((DisabledException)exception).getUserId());
                response.sendRedirect("/update-password");
            }
            else {
                response.sendRedirect("/login");
            }
        }

        return;
    }
}