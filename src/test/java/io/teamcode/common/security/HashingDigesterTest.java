package io.teamcode.common.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 2. 28..
 */
public class HashingDigesterTest {

    @Test
    public void generateHtDigest() {
        String expected = "chiang:Teamcode Managed Subversion Server:26b1b80a9597369c9b1c9f76fc7e30d3";
        String encrypted = HashingDigester.generateHtDigest("chiang", "pero", "Teamcode Managed Subversion Server");

        Assert.assertEquals(expected, encrypted);

        //expected = "chiang:Teamcode Managed Subversion Server:26b1b80a9597369c9b1c9f76fc7e30d3";
        encrypted = HashingDigester.generateHtDigest("leesy8", "1234", "Visang Subversion Repository");
        System.out.println("--> "  + encrypted);

        encrypted = HashingDigester.generateHtDigest("ryan", "gosling", "TeamCode Managed Subversion Repository");
        System.out.println("--> "  + encrypted);


        //신형수 선생님
        //  shs!@861109
        encrypted = HashingDigester.generateHtDigest("hsshin85", "shs!@861109", "TeamCode Managed Subversion Server");
        System.out.println("--> "  + encrypted);
        //Assert.assertEquals(expected, encrypted);
    }
}
