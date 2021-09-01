package io.teamcode.web.security;

import org.springframework.security.core.AuthenticationException;

public class DisabledException extends AuthenticationException {

    private String userId;

    public DisabledException(String msg, String userId) {
        super(msg);
        this.userId = userId;
    }

    public DisabledException(String msg, Throwable t) {
        super(msg, t);
    }

    public DisabledException(String msg) {
        super(msg);
    }

    public String getUserId() {
        return this.userId;
    }

}
