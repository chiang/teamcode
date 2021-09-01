package io.teamcode.common.vcs.svn;

import io.teamcode.TeamcodeException;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.vcs.VcsCommunicationFailureException;
import io.teamcode.common.vcs.svn.callback.SvnLogMessageCallback;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.visang.VisangRmsRecord;
import io.teamcode.domain.entity.visang.VisangRmsRecordDetails;
import io.teamcode.util.DateUtils;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.CommitItem;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.SVNClient;
import org.apache.subversion.javahl.callback.CommitMessageCallback;
import org.apache.subversion.javahl.callback.InfoCallback;
import org.apache.subversion.javahl.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chiang on 2017. 4. 3..
 */
public abstract class SvnCommandHelper {

    private static final Logger logger = LoggerFactory.getLogger(SvnCommandHelper.class);

    /**
     * Parameters:
     path - the local path
     url - the target url
     depth - depth to traverse into subdirectories
     noIgnore - whether to add files matched by ignore patterns
     ignoreUnknownNodeTypes - whether to ignore files which the node type is not konwn, just as pipes
     revpropTable - A string-to-string mapping of revision properties to values which will be set if this operation results in a commit.
     handler - the commit message callback
     callback - the commit status callback
     *
     */
    public static final void doImport(final File tempDir, final String repositoryUri, final String content, final String message) throws IOException {
        String[] yearMonth = DateUtils.dateBasedDirectoryStructure();
        File tempYear = new File(tempDir, yearMonth[0]);
        File tempMonth = new File(tempYear, yearMonth[1]);
        if (!tempMonth.exists()) {
            if (!tempMonth.mkdirs()) {
                throw new IOException(String.format("임시 디렉터리 '%s' 를 생성할 수 없습니다.", tempMonth.getAbsolutePath()));
            }
        }

        File tempFile = new File(tempMonth, Long.valueOf(new Date().getTime()).toString());
        FileWriter fileWriter = new FileWriter(tempFile);
        fileWriter.write(content);
        fileWriter.flush();

        ISVNClient client = null;
        try {
            client = new SVNClient();

            //Map<String, String> revpropTable = new HashMap<>();
            //revpropTable.put("svn:log", message);

            client.doImport(tempFile.getAbsolutePath(), repositoryUri, Depth.infinity, false, true, Collections.emptyMap(), set -> message, commitInfo -> {
                logger.debug("Committed revision: {}", commitInfo.getRevision());
                if (StringUtils.hasText(commitInfo.getPostCommitError()))
                    logger.warn("Post commit error: {}", commitInfo.getPostCommitError());
            });
            logger.debug("파일 '{}' 의 내용을 커밋했습니다.", tempFile.getAbsolutePath());
        } catch (ClientException e) {
            logger.error("svn import 명령어를 실행 중 오류가 발생했습니다.", e);
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();

            fileWriter.close();
        }

    }

    public static final long getHeadRevisionNumber(final String repositoryUri) {
        ISVNClient client;
        client = new SvnClientFactory().createLocalClient();
        try {
            AtomicReference<Long> revisionReference = new AtomicReference<>();
            client.info2(repositoryUri, Revision.HEAD, Revision.HEAD, Depth.immediates, Collections.emptyList(), new InfoCallback() {
                @Override
                public void singleInfo(Info info) {
                    revisionReference.set(info.getRev());
                }
            });

            return revisionReference.get();
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);
            throw new SvnCommandException();
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    /*public static final String getPipelineYamlContent(final String repositoryUri, final String projectPath) {


        String pipelineYamlUri = SvnUtils.buildAbsoluteUrlAsFileScheme(repositoryUri, projectPath, TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SvnCommandHelper.getContent(pipelineYamlUri, outputStream);

        try {
            return new String(outputStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(outputStream.toByteArray());
        }
    }*/

    public static final String getPipelineYmlContent(final Project project, final File repositoryRootDir) {
        String relativePath = String.format("%s%s", project.getPath(), project.getResolvedPipelineConfigPath());
        String ymlPath = SvnUtils.buildAbsoluteUrlAsFileScheme(repositoryRootDir.getAbsolutePath(), relativePath, TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

        return getContent(ymlPath);
    }

    public static final String getContent(final String ymlRepositoryUri) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SvnCommandHelper.getContent(ymlRepositoryUri, outputStream);

        try {
            return new String(outputStream.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(outputStream.toByteArray());
        }
    }

    public static final void getContent(final String repositoryUri, final OutputStream outputStream) {
        ISVNClient client = null;
        try {
            client = new SVNClient();

            client.streamFileContent(repositoryUri, Revision.HEAD, Revision.HEAD, outputStream);
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }



    public static final boolean exist(final String repositoryUri) {
        ISVNClient client;
        client = new SvnClientFactory().createLocalClient();
        try {
            Revision pegRevision = null;
            int direntFields = -1;
            boolean fetchLocks = false;
            client.list(repositoryUri, Revision.HEAD, pegRevision, Depth.immediates, direntFields, fetchLocks, (dirEntry, lock) -> {
                //do nothing...
            });

            return true;
        } catch (ClientException e) {
            if (SvnExceptionHandler.isFileNotFoundError(e)) {
                return false;
            }

            SvnExceptionHandler.throwDetailSvnException(e);
            throw new SvnCommandException();
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    /**
     * 서브버전 UUID. 이 값은 저장소 별로 생성되는 것이 아니라 서브버전 호스트별로 생성되는 것임.
     *
     *
     * @param repositoryUri
     * @return
     */
    public static final String getUUID(final String repositoryUri) {
        ISVNClient client;
        client = new SvnClientFactory().createLocalClient();
        try {
            AtomicReference<String> uuidReference = new AtomicReference<>();
            client.info2(repositoryUri, Revision.HEAD, Revision.HEAD, Depth.immediates, Collections.emptyList(), new InfoCallback() {
                @Override
                public void singleInfo(Info info) {
                    uuidReference.set(info.getReposUUID());
                }
            });

            return uuidReference.get();
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);
            throw new SvnCommandException();
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    public static final void addRevisionProperty(final String repositoryUri, final Long revisionNumber, final String propertyName, final String propertyValue) {
        ISVNClient client = null;
        try {
            client = new SvnClientFactory().createLocalClient();
            boolean force = true;
            Revision revision = Revision.getInstance(revisionNumber);
            client.setRevProperty(repositoryUri, propertyName, revision, propertyValue, null, force);
        } catch (ClientException e) {
            //비동기이므로 에러가 나도 그냥 넘긴다.
            //FIXME 무조건 Commit은 되게 해야 겠지만, 그래도 에러를 추적해야 하지 않나?
            logger.error("cannot add changeset by commit hook", e);
            throw new TeamcodeException(e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }
}
