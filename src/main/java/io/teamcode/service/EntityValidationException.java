package io.teamcode.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 입력된 값에 문제가 있는 경우 전달합니다.
 */
@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class EntityValidationException extends RuntimeException {

    public EntityValidationException() {
        super();
    }

    public EntityValidationException(String s) {
        super(s);
    }
}
