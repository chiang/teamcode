package io.teamcode.domain;

import io.teamcode.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by chiang on 16. 3. 11..
 */
public class TcUserDetails extends org.springframework.security.core.userdetails.User {

    private User domainUser;

    public TcUserDetails(String username, String password,
                         boolean enabled, boolean accountNonExpired,
                         boolean credentialsNonExpired, boolean accountNonLocked,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
    }

    public User getDomainUser() {
        return domainUser;
    }

    public void setDomainUser(User domainUser) {
        this.domainUser = domainUser;
    }
}
