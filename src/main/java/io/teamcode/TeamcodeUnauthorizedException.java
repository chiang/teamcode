package io.teamcode;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by chiang on 2017. 2. 28..
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TeamcodeUnauthorizedException extends TeamcodeSecurityException {

    public TeamcodeUnauthorizedException() {
        super();
    }

    public TeamcodeUnauthorizedException(String s) {
        super(s);
    }

    public TeamcodeUnauthorizedException(Throwable t) {
        super(t);
    }

    public TeamcodeUnauthorizedException(String s, Throwable t) {
        super(s, t);
    }

}
