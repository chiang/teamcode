package io.teamcode.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Service
@Transactional(readOnly = true)
public class ImpersonateService {

    private static final Logger logger = LoggerFactory.getLogger(ImpersonateService.class);

    @Autowired
    UserDetailsService tcUserDetailsService;

    public void switchAdmin() {
        SecurityContextHolder.getContext().setAuthentication(attemptSwitchUser("admin"));
    }

    public void clearAuthentiation() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    protected Authentication attemptSwitchUser(final String username)
            throws AuthenticationException {
        UsernamePasswordAuthenticationToken targetUserRequest;

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Attempt to switch to user [{}]", username);
        }

        UserDetails targetUser = this.tcUserDetailsService.loadUserByUsername(username);

        // OK, create the switch user token
        targetUserRequest = createSwitchUserToken(targetUser);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Switch User Token [{}]", targetUserRequest.getName());
        }

        return targetUserRequest;
    }

    private UsernamePasswordAuthenticationToken createSwitchUserToken(UserDetails targetUser) {

        UsernamePasswordAuthenticationToken targetUserRequest;

        // grant an additional authority that contains the original Authentication object
        // which will be used to 'exit' from the current switched user.

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        GrantedAuthority switchAuthority = new SwitchUserGrantedAuthority("ROLE_ADMIN", currentAuth);

        // get the original authorities
        Collection<? extends GrantedAuthority> orig = targetUser.getAuthorities();

        // add the new switch user authority
        List<GrantedAuthority> newAuths = new ArrayList<GrantedAuthority>(orig);
        newAuths.add(switchAuthority);

        // create the new authentication token
        targetUserRequest = new UsernamePasswordAuthenticationToken(targetUser,
                targetUser.getPassword(), newAuths);

        return targetUserRequest;
    }
}
