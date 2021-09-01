package io.teamcode.common.vcs.svn;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 4. 8..
 */
public class SvnCommandHelperTest {

    @Test
    public void exist() {
        Assert.assertFalse(SvnCommandHelper.exist("file:///Users/chiang/teamcode/data/repositories/example/abc.yaml"));
    }
}
