package io.teamcode.common.vcs.svn;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by chiang on 2017. 2. 28..
 */
public class SvnSecurityHelperTest {

    @Test
    public void addOrUpdateUser() throws IOException {
        String realm = "Teamcode Managed Subversion Server";

        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File passwordFile = new File(baseDir, "svn-users");
        passwordFile.createNewFile();
        SvnSecurityHelper.addOrUpdateUser(realm, passwordFile.getAbsolutePath(), "chiang", "pero", true);
        SvnSecurityHelper.addOrUpdateUser(realm, passwordFile.getAbsolutePath(), "pero", "chiang", true);

        File prevFile = new File(baseDir, passwordFile.getName() + ".prev");
        Assert.assertTrue(prevFile.exists());

        List<String> lines = FileUtils.readLines(passwordFile, Charset.defaultCharset());
        Assert.assertEquals(2, lines.size());

        String expected = "chiang:Teamcode Managed Subversion Server:26b1b80a9597369c9b1c9f76fc7e30d3";
        lines.stream().filter(l -> l.startsWith("chiang")).forEach(l -> {
            Assert.assertEquals(expected, l);
        });

        SvnSecurityHelper.deleteUser(passwordFile.getAbsolutePath(), "pero");
        lines = FileUtils.readLines(passwordFile, Charset.defaultCharset());
        Assert.assertEquals(1, lines.size());
        lines.stream().filter(l -> l.startsWith("chiang")).forEach(l -> {
            Assert.assertEquals(expected, l);
        });
    }

    @Test
    public void addUserToRepository() throws IOException {
        File authzFile = new File("authz");
        if (!authzFile.exists())
            if (!authzFile.createNewFile())
                throw new IOException();

        //TODO
        /*SvnSecurityHelper.updatePermission(authzFile.getAbsolutePath(), "chiang", "petclinic", AuthzPrivilege.READ_ONLY);
        SvnSecurityHelper.updatePermission(authzFile.getAbsolutePath(), "chiang", "petclinic", AuthzPrivilege.DENY_ACCESS);

        SvnSecurityHelper.updatePermission(authzFile.getAbsolutePath(), "chiang", "bootstrap", AuthzPrivilege.READ_WRITE);
        SvnSecurityHelper.updatePermission(authzFile.getAbsolutePath(), "chiang", "bootstrap", AuthzPrivilege.READ_ONLY);
        SvnSecurityHelper.updatePermission(authzFile.getAbsolutePath(), "pero", "bootstrap", AuthzPrivilege.READ_WRITE);*/
    }
}
