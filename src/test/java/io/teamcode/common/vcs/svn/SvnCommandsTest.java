package io.teamcode.common.vcs.svn;

import io.teamcode.common.vcs.svn.callback.SvnListCallback;
import io.teamcode.common.vcs.svn.callback.SvnLogMessageCallback;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.callback.InfoCallback;
import org.apache.subversion.javahl.types.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 17..
 */
public class SvnCommandsTest {

    private SvnClientFactory svnClientFactory = new SvnClientFactory();

    //private static final String repositoryUrl = "http://svn.rightstack.net:9494/repos/bootstrap";
    private static final String repositoryUrl = "http://localhost:9999/repos/bootstrap";

    @Test
    public void info() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient("lala", "land");
            client.info2(repositoryUrl, Revision.HEAD, Revision.HEAD, Depth.immediates, Collections.emptyList(), new InfoCallback(){

                @Override
                public void singleInfo(Info info) {
                    System.out.println("last revision: " + info.getLastChangedRev());
                }
            });
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    @Test
    public void list() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient("lala", "land");
            SvnListCallback callback = new SvnListCallback();
            client.list(repositoryUrl, Revision.HEAD, null, Depth.immediates, DirEntry.Fields.all, true, callback);

            List<RepositoryItem> repositoryItems = callback.getRepositoryItems();
            Assert.assertEquals(3, repositoryItems.size());
        } catch (ClientException e) {
            if (client != null)
                client.dispose();

            throw e;
        }
    }

    @Test
    public void listLocal() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient();
            SvnListCallback callback = new SvnListCallback();
            client.list("file:///Users/chiang/teamcode-home/repositories/example", Revision.HEAD, null, Depth.immediates, DirEntry.Fields.all, true, callback);

            List<RepositoryItem> repositoryItems = callback.getRepositoryItems();
            Assert.assertEquals(3, repositoryItems.size());
        } catch (ClientException e) {
            if (client != null)
                client.dispose();

            throw e;
        }
    }

    @Test
    public void cat() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient("lala", "land");
            byte[] content = client.fileContent(String.format("%s/trunk/README.txt", repositoryUrl), Revision.HEAD, Revision.BASE);
            Assert.assertTrue(content.length > 10);
        } catch (ClientException e) {
            if (client != null)
                client.dispose();

            throw e;
        }

    }

    @Test
    public void logMessages() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient("lala", "land");

            boolean stopOnCopy = false;
            boolean discoverPath = true;
            boolean includeMergedRevisions = false;
            long limit = 0l;

            SvnLogMessageCallback callback = new SvnLogMessageCallback();
            client.logMessages(repositoryUrl, null, SvnUtils.headRevisionRanages(),
                    stopOnCopy, discoverPath, includeMergedRevisions, SvnUtils
                            .defaultRevisionProps(), limit, callback);
            List<Commit> changeSets = callback.getRepositoryHistories();
            String path;
            for (Commit cs : changeSets) {
                List<ChangedFile> changedFiles = cs.getChangedFiles();
                for (ChangedFile cf : changedFiles) {
                    //cf.get
                }
            }
        } catch (ClientException e) {
            if (client != null)
                client.dispose();

            throw e;
        }
    }

    @Test
    public void diff() throws ClientException {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient("lala", "land");


            boolean stopOnCopy = false;
            boolean discoverPath = true;
            boolean includeMergedRevisions = false;
            long limit = 0l;

            String repositoryUri = String.format("%s/trunk/README.md", repositoryUrl);
            SvnLogMessageCallback callback = new SvnLogMessageCallback();
            Revision pegRevision = null;
            Revision baseRevision = Revision.getInstance(8);
            Revision endRevision = Revision.PREVIOUS;
            String relativeToDir = null;
            Collection<String> changelists = Collections.emptyList();
            boolean ignoreAncestry = true;
            boolean noDiffDeleted = true;
            boolean force = false;
            boolean copiesAsAdds = false;
            boolean ignoreProps = true;
            boolean propsOnly = false;
            /* DiffOptions options = DiffOptions.Flag; */
            DiffOptions diffOptions = new DiffOptions();

            OutputStream outputStream = new ByteArrayOutputStream();

            client.diff(repositoryUri,
                    pegRevision,
                    baseRevision,
                    endRevision,
                    relativeToDir,
                    outputStream,
                    Depth.infinity, changelists, ignoreAncestry,
                    noDiffDeleted, force, copiesAsAdds, ignoreProps, propsOnly, null);

            OutputStream outputStream2 = new ByteArrayOutputStream();
            /*void diff(String var1, Revision var2, String var3,
                    Revision var4, String var5,
                    OutputStream var6, Depth var7,
                    Collection<String> var8,
            boolean var9, boolean var10, boolean var11, boolean var12, boolean var13, boolean var14, DiffOptions var15) throws ClientException;*/
            //client.diff(repositoryUri, baseRevision, null, null, outputStream2, Depth.infinity, changelists, ignoreAncestry,
//                    noDiffDeleted, force, copiesAsAdds, ignoreProps, propsOnly, null);

            System.out.println("--->>> " + outputStream.toString());
        } catch (ClientException e) {
            if (client != null)
                client.dispose();

            throw e;
        }
    }

    @Test
    public void directDiff() throws IOException {
        String repositoryUril = "file:///Users/chiang/teamcode-home/repositories/example";
        String diff = SvnCommandExecutor.diff(repositoryUril, 24);
        System.out.println("--> " + diff);
    }
}
