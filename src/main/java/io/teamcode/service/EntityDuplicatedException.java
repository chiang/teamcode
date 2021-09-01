package io.teamcode.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 */
@ResponseStatus(value= HttpStatus.CONFLICT)
public class EntityDuplicatedException extends RuntimeException {

    public EntityDuplicatedException() {
        super();
    }

    public EntityDuplicatedException(String s) {
        super(s);
    }
}
