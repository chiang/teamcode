package io.teamcode.common;

import org.apache.subversion.javahl.types.Revision;
import org.junit.Test;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class RevisionTest {

    @Test
    public void revision() {
        long revisionNumber = 203123;

        Revision revision = Revision.getInstance(revisionNumber);
        System.out.println("--> " + revision.toString());

    }
}
