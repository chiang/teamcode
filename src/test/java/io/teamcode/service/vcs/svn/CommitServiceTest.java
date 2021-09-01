package io.teamcode.service.vcs.svn;

import io.teamcode.common.vcs.svn.Commit;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.config.TcConfig;
import io.teamcode.util.FileSystemUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;

import java.io.File;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CommitServiceTest {

    @Mock
    TcConfig tcConfig;

    @Mock
    SvnClientFactory svnClientFactory;

    //@Mock 으로 Mock 객체를 만든 것을 @InjectMocks 로 선언한 대상에 주입한다. 즉, broadcastMessageRepository 을 broadcastMessageService에 주입한다.
    @InjectMocks
    CommitService repositoryHistoryService;

    @Test
    public void list() {
        File repositoryRootDir = new File(FileSystemUtils.getDefaultHomeDir(), "repositories");

        Mockito.when(tcConfig.getRepositoryRootDir()).thenReturn(repositoryRootDir);
        Mockito.when(tcConfig.getRepositoryRootPath()).thenReturn(repositoryRootDir.getAbsolutePath());
        //for multiple call createLocalClient (이렇게 안하면 JNI 오류가 발생한다.
        Mockito.when(svnClientFactory.createLocalClient())
                .thenReturn(new SvnClientFactory().createLocalClient(), new SvnClientFactory().createLocalClient());

        Page<Commit> repositoryHistories = repositoryHistoryService.list("example", -1, 5);
        Assert.assertEquals(5, repositoryHistories.getNumberOfElements());
        //Assert.assertArrayEquals(new long[]{1, 2, 3, 4, 5}, repositoryHistories.stream().mapToLong(r -> r.getRevision()).toArray());

        repositoryHistories = repositoryHistoryService.list("example", 5, 5);
    }
}
