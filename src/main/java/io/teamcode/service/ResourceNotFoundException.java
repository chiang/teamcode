package io.teamcode.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by chiang on 2017. 1. 18..
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such resource")
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String s) {
        super(s);
    }
}
