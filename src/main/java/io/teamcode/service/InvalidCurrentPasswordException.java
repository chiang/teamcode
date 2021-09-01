package io.teamcode.service;

/**
 * Created by chiang on 2017. 2. 3..
 */
public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        //super("You must provide a valid current password.");
        super("현재 비밀번호가 맞지 않습니다.");
    }

    public InvalidCurrentPasswordException(String s) {
        super(s);
    }
}
