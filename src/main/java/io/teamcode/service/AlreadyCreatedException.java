package io.teamcode.service;

/**
 * Created by chiang on 2017. 2. 27..
 */
public class AlreadyCreatedException extends RuntimeException {

    public AlreadyCreatedException() {
        super();
    }

    public AlreadyCreatedException(String s) {
        super(s);
    }
}
