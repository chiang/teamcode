package io.teamcode.common.io;

import org.apache.commons.exec.ExecuteException;

/**
 * Created by chiang on 2017. 5. 24..
 */
public class KnownFilesResolveException extends Exception {

    public KnownFilesResolveException(String s, Throwable t) {
        super(s, t);
    }
}
