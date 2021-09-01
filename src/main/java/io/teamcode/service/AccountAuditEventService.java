package io.teamcode.service;

import eu.bitwalker.useragentutils.UserAgent;
import io.teamcode.domain.entity.AccountAuditEvent;
import io.teamcode.domain.entity.User;
import io.teamcode.repository.AccountAuditEventRepository;
import io.teamcode.repository.UserRepository;
import io.teamcode.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by chiang on 2017. 3. 5..
 */
@Service
@Transactional(readOnly = true)
public class AccountAuditEventService {

    private static final Logger logger = LoggerFactory.getLogger(AccountAuditEventService.class);

    @Autowired
    AccountAuditEventRepository accountAuditEventRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional
    public void createLoginEvent(final HttpServletRequest request, final User loginUser) {
        logger.debug("사용자 '{}' 의 접속 이력을 저장합니다...", loginUser.getName());

        String userAgentString = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        AccountAuditEvent accountAuditEvent = new AccountAuditEvent();
        accountAuditEvent.setUser(loginUser);
        accountAuditEvent.setIpAddress(ServletUtils.getRealIpAddress(request));
        accountAuditEvent.setUserAgentId(userAgent.getId());
        accountAuditEvent.setBrowser(userAgent.getBrowser());
        accountAuditEvent.setOperatingSystem(userAgent.getOperatingSystem());
        accountAuditEvent.setUserAgentString(userAgentString);

        accountAuditEventRepository.save(accountAuditEvent);

        loginUser.plusSignInCount();
        if (StringUtils.hasText(loginUser.getCurrentSignInIp()))
            loginUser.setLastSignInIp(new String(loginUser.getCurrentSignInIp().getBytes()));
        loginUser.setLastSignInAt(loginUser.getCurrentSignInAt());

        loginUser.setCurrentSignInIp(accountAuditEvent.getIpAddress());
        loginUser.setCurrentSignInAt(new Date());

        userRepository.save(loginUser);
        logger.debug("사용자 로그인 정보를 업데이트했습니다.");
    }
}
