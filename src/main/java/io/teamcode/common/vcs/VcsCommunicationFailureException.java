package io.teamcode.common.vcs;

/**
 * Created by chiang on 2017. 3. 18..
 */
public class VcsCommunicationFailureException extends RuntimeException {

    public VcsCommunicationFailureException(String s) {
        super(s);
    }

    public VcsCommunicationFailureException(String s, Throwable t) {
        super(s, t);
    }
}
