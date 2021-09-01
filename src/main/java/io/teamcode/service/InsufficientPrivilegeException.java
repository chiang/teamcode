package io.teamcode.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by chiang on 2017. 2. 1..
 */
@ResponseStatus(value= HttpStatus.FORBIDDEN, reason="No such resource")
public class InsufficientPrivilegeException extends RuntimeException {

    public InsufficientPrivilegeException() {
        super();
    }

    public InsufficientPrivilegeException(String s) {
        super(s);
    }
}
