package io.teamcode.service;

/**
 * Created by chiang on 2017. 2. 3..
 */
public class EmailAlreadyExistException extends RuntimeException {

    public EmailAlreadyExistException() {
        super("Email has already been taken.");
    }

}
