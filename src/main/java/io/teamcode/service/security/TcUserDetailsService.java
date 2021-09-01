package io.teamcode.service.security;

import io.teamcode.domain.TcUserDetails;
import io.teamcode.domain.entity.User;
import io.teamcode.domain.entity.UserRole;
import io.teamcode.domain.entity.UserState;
import io.teamcode.service.UserService;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 16. 3. 11..
 */
@Service
@Transactional(readOnly =  true)
public class TcUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(TcUserDetailsService.class);

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        logger.debug("Loading user details by user name: {}", username);

        try {
            User domainUser = userService.get(username);

            if (domainUser == null) {
                throw new UsernameNotFoundException("Cannot find user '" + username + "'.");
            }

            boolean enabled = domainUser.getState() == UserState.ACTIVE;
            boolean accountNonExpired = true;//TODO
            boolean credentialsNonExpired = true;//TODO
            boolean accountNonLocked = true;//TODO

            TcUserDetails userDetails = new TcUserDetails(
                    domainUser.getName(),
                    domainUser.getPassword(),
                    enabled,
                    accountNonExpired,
                    credentialsNonExpired,
                    accountNonLocked,
                    getAuthorities(new String[]{domainUser.getUserRole().name()}));
            userDetails.setDomainUser(domainUser);

            return userDetails;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a collection of {@link GrantedAuthority} based on a numerical role
     * @param roles the numerical role
     * @return a collection of {@link GrantedAuthority
     */
    public Collection<? extends GrantedAuthority> getAuthorities(String[] roles) {
        List<GrantedAuthority> authList = getGrantedAuthorities(getRoles(roles));
        return authList;
    }

    /**
     * Converts a numerical role to an equivalent list of roles
     * @param roles the numerical role
     * @return list of roles as as a list of {@link String}
     */
    public List<String> getRoles(String[] roles) {
        List<String> roleNames = new ArrayList<>();
        for (String role: roles) {
            roleNames.add(role);
        }

        return roleNames;
    }

    /**
     * Wraps {@link String} roles to {@link SimpleGrantedAuthority} objects
     * @param roles {@link String} of roles
     * @return list of granted authorities
     */
    public List<GrantedAuthority> getGrantedAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            //if ("ROLE_ADMIN".equals(role)) {
             //   authorities.add(new SimpleGrantedAuthority("ROLE_ACTUATOR"));
           // }
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
}
