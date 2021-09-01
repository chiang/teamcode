package io.teamcode.web.security;

import io.teamcode.config.TcConfig;
import io.teamcode.domain.TcUserDetails;
import io.teamcode.domain.entity.User;
import io.teamcode.service.security.TcUserDetailsService;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Component
public class TeamcodeAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(TeamcodeAuthenticationProvider.class);

    @Autowired
    TcUserDetailsService tcUserDetailsService;

    @Autowired
    @Qualifier("tcPasswordEncoder")
    PasswordEncoder passwordEncoder;

    @Autowired
    TcConfig tcConfig;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        TcUserDetails user;
        Collection<? extends GrantedAuthority> authorities;
        try {
            user = (TcUserDetails)tcUserDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, user.getPassword()))
                throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");

            validateSecurityPolicy(user.getDomainUser());

            authorities = user.getAuthorities();

            Set<String> roles = AuthorityUtils.authorityListToSet(authorities);
            if (roles.contains("ROLE_ADMIN") && tcConfig.getIpWhitelist().length > 0) {
                logger.debug("접속 시도한 사용자의 Role 이 관리자입니다. Whitelist 기반 접근 제어를 시작합니다...");
                String userIp = ((WebAuthenticationDetails)authentication.getDetails()).getRemoteAddress();
                logger.debug("접속을 시도한 IP 주소: {}", userIp);

                boolean matches = false;
                for (String s: tcConfig.getIpWhitelist()) {
                    logger.debug("000>>> [{}]", s);
                    if (s.equals(userIp)) {
                        matches = true;
                    }
                }
                if(!matches){
                    throw new BadCredentialsException("접속이 제한된 곳에서 접속하셨습니다. 다시 확인 후 시도해 주세요.");
                }
            }

        } catch(UsernameNotFoundException e) {
            logger.info(e.toString());
            throw new UsernameNotFoundException(e.getMessage());
        } catch(BadCredentialsException e) {
            logger.info(e.toString());
            throw new BadCredentialsException(e.getMessage());
        } catch (DisabledException e) {

          throw e;
        } catch(Exception e) {
            logger.info(e.toString());
            throw new RuntimeException(e.getMessage());
        }

        return new UsernamePasswordAuthenticationToken(user, password, authorities);
    }

    //TODO spring security Decision 을 써서 고급스럽게?
    private final void validateSecurityPolicy(User domainUser) {
        logger.debug("사용자 계정 보안 정책을 검사합니다...");
        Date lastSignInAt = domainUser.getLastSignInAt();
        Period period = new Period(new DateTime(lastSignInAt), DateTime.now());
        logger.debug("마지막 로그인한 날짜로부터 경과 월: {}", period.getMonths());

        //visang, 관리자 계정도 1개월 이상 접근하지 않는 경우가 있어 isAdmin 조건 추가.
        if (period.getMonths() >= 1 && !domainUser.isAdmin()) {
            throw new BadCredentialsException("1개월 이상 로그인하지 않아 계정이 잠겼습니다. 관리자에게 문의 주세요.");
        }

        Date lastPasswordModifiedAt = domainUser.getLastPasswordModifiedAt();
        period = new Period(new DateTime(lastPasswordModifiedAt), DateTime.now());
        logger.debug("마지막 비밀번호 변경일로부터 경과 월: {}", period.getMonths());
        if (period.getMonths() >= 3) {
            DisabledException e = new DisabledException("3개월 이상 비밀번호를 변경하지 않았습니다. 비밀번호 변경 후 이용하시기 바랍니다.", domainUser.getName());
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> arg0) {
        return true;
    }


}
