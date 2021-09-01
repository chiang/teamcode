package io.teamcode;

/**
 * Created by chiang on 2017. 2. 28..
 */
public class TeamcodeException extends RuntimeException  {

    public TeamcodeException() {
        super();
    }

    public TeamcodeException(String s) {
        super(s);
    }

    public TeamcodeException(Throwable t) {
        super(t);
    }

    public TeamcodeException(String s, Throwable t) {
        super(s, t);
    }
}
