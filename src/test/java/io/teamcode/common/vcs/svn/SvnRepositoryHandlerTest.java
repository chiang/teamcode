package io.teamcode.common.vcs.svn;

import io.teamcode.util.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by chiang on 2017. 3. 17..
 */
public class SvnRepositoryHandlerTest {

    @Test
    public void createRepository() throws IOException {
        File tcHomeDir = FileSystemUtils.getDefaultHomeDir();
        File repositoryRootDir = new File(FileSystemUtils.getDefaultHomeDir(), "repositories");
        File repositoryDir = new File(repositoryRootDir, "petclinic");
        FileUtils.deleteDirectory(repositoryDir);

        SvnRepositoryHandler.createRepositoryFromTemplate("1.8", "http://teamcode.example.com", tcHomeDir, repositoryDir, false);
        FileUtils.deleteDirectory(repositoryDir);
    }
}
