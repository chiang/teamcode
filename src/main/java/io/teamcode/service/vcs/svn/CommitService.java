package io.teamcode.service.vcs.svn;

import com.google.common.collect.Sets;
import io.teamcode.common.vcs.DiffParser;
import io.teamcode.common.vcs.VcsCommunicationFailureException;
import io.teamcode.common.vcs.svn.*;
import io.teamcode.common.vcs.svn.callback.SimpleLogMessage;
import io.teamcode.common.vcs.svn.callback.SimpleLogMessageCallback;
import io.teamcode.common.vcs.svn.callback.SvnLogMessageCallback;
import io.teamcode.common.vcs.svn.diff.SvnDiffParser;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ProjectRole;
import io.teamcode.domain.entity.User;
import io.teamcode.repository.UserRepository;
import io.teamcode.service.InsufficientPrivilegeException;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.UserService;
import io.teamcode.service.project.integration.CustomIssueTracker;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.CommitInfo;
import org.apache.subversion.javahl.CommitItem;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.types.Revision;
import org.apache.subversion.javahl.types.RevisionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by chiang on 2017. 3. 19..
 */
@Component
public class CommitService {

    private static final Logger logger = LoggerFactory.getLogger(CommitService.class);

    private static final long DEFAULT_LIMIT = 10;

    @Autowired
    TcConfig tcConfig;

    @Autowired
    SvnClientFactory svnClientFactory;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    public Commit getCommit(final String projectPath, final long revisionNumber) {

        return getCommit(projectPath, Revision.getInstance(revisionNumber));
    }

    public Commit getCommit(final String projectPath, final Revision revision) {
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "");
        logger.debug("???????????? '{}' ??? ?????? r'{}' ??? '{}' ?????? ???????????????...", projectPath, revision, repositoryUri);

        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient();

            boolean stopOnCopy = false;
            boolean discoverPath = true;
            boolean includeMergedRevisions = false;
            long limit = 1;
            Set<String> revisionProps = SvnUtils.defaultRevisionProps();

            SimpleLogMessageCallback simpleLogMessageCallback = new SimpleLogMessageCallback();
            client.logMessages(repositoryUri, revision, SvnUtils.oneRevisionRanages(revision),
                    stopOnCopy, discoverPath, includeMergedRevisions, revisionProps, limit, simpleLogMessageCallback);

            SimpleLogMessage simpleLogMessage = simpleLogMessageCallback.getSimpleLogMessage();
            Commit commit = new Commit();
            commit.setRevision(simpleLogMessage.getRevisionNumber());
            commit.setAuthor(simpleLogMessage.getAuthor());
            commit.setMessage(simpleLogMessage.getMessage());
            commit.setCommitPath(new StringBuilder("/projects/").append(projectPath).append("/commits/").append(commit.getRevision()).toString());
            commit.setCreatedAt(simpleLogMessage.getCommitedAt());
            commit.setUser(userRepository.findByName(commit.getAuthor()));

            logger.debug("???????????? '{}' ??? ????????? ?????? ????????? ??????????????????. ????????? ?????? ??????: {}", projectPath, commit.getCreatedAt());

            return commit;
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    //TODO chagedFile ??? ???????????? ???????????? ??????. ????????? ????????????
    public Page<Commit> list(final String projectPath, final long offset, long limit) {
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "");

        ISVNClient client = null;
        Page<Commit> page;
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("svn log for project: {}", projectPath);
                logger.trace("svn log by start revision number: {}", (offset + 1));
                logger.trace("svn log by limit: {}", limit);
            }


            long headRevisionNumber = SvnCommandHelper.getHeadRevisionNumber(repositoryUri);

            client = svnClientFactory.createLocalClient();

            boolean stopOnCopy = false;
            boolean discoverPath = true;
            boolean includeMergedRevisions = false;
            List<RevisionRange> revisionRanges = offset < 1 ? SvnUtils.defaultRevisionRanges() : SvnUtils.revisionRanges(offset - 1);
            Set<String> revisionProps = SvnUtils.defaultRevisionProps(Sets.newHashSet("rms:id", CustomIssueTracker.REV_PRO_NAME));
            limit = limit < 0 ? DEFAULT_LIMIT : limit;

            SvnLogMessageCallback callback = new SvnLogMessageCallback(revisionProps);
            client.logMessages(repositoryUri, null, revisionRanges,
                    stopOnCopy, discoverPath, includeMergedRevisions, revisionProps, limit, callback);

            List<Commit> commits = callback.getRepositoryHistories();
            commits.stream().forEach(c -> c.setUser(userRepository.findByName(c.getAuthor())));

            page = new CommitsPageImpl(commits, (int)DEFAULT_LIMIT, headRevisionNumber);

            return page;
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }

    }

    public CommitDetails getCommitDetails(final String projectPath, final long revisionNumber) {
        ISVNClient client = null;

        SvnLogMessageCallback callback = new SvnLogMessageCallback();

        try {
            client = svnClientFactory.createLocalClient();
            String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "");
            //logMessages(String path, Revision pegRevision, List<RevisionRange> revisionRanges, boolean stopOnCopy, boolean discoverPath, boolean includeMergedRevisions, Set<String> revProps, long limit, LogMessageCallback callback)

            Revision revision = Revision.getInstance(revisionNumber);
            client.logMessages(repositoryUri, null,
                    SvnUtils.oneRevisionRanages(revisionNumber),
                    false, true, false,
                    SvnUtils.defaultRevisionProps(new HashSet<>(Arrays.asList("svn:mime-type"))),
                    0L, callback);

            CommitDetails commitDetails = new CommitDetails();

            //User changeSetOwner = null;
            List<Commit> repositoryHistories = callback.getRepositoryHistories();

            //Single Revision ??? ???????????? ????????? ????????? ????????? ??????.
            //FIXME ?????? Revision ??? ?????? ?????? ??????.
            if (repositoryHistories.size() == 1) {
                Commit commit = repositoryHistories.get(0);
                commitDetails.setMessage(commit.getMessage());
                commitDetails.setAuthor(commit.getAuthor());
                commitDetails.setRevision(revisionNumber);
                commitDetails.setCreatedAt(commit.getCreatedAt());

                for (ChangedFile changedFile: commit.getChangedFiles()) {
                    if (changedFile.getName().toLowerCase().endsWith(".pdf")) {
                        commitDetails.addDiffs(Arrays.asList(new Diff(changedFile.getPath(),true)));
                    }
                    else {
                        commitDetails.addDiffs(diff(new StringBuilder(repositoryUri).append(changedFile.getPath()).toString(), revisionNumber));
                    }
                }

                Collections.sort(commitDetails.getDiffs());
            }
            else {
                //TODO custom exception?
                throw new ResourceNotFoundException(String.format("????????? Revision '{}' ??? ?????? ??? ????????????.", revisionNumber));
            }

            resolveImageTag(commitDetails, projectPath);
            return commitDetails;
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);
            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } catch (IOException e) {
            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            client.dispose();
        }
    }

    private List<Diff> diff(String repositoryUri, long revisionNumber) throws IOException {
        logger.debug("?????? '{}' ??? ?????? diff ??? ???????????????...", repositoryUri);
        String diff = SvnCommandExecutor.diff(repositoryUri, revisionNumber);

        DiffParser parser = new SvnDiffParser();
        return parser.parse(diff.getBytes());
    }

    private void resolveImageTag(final CommitDetails commitDetails, final String projectPath) {
        commitDetails.getDiffs().stream().forEach(d -> {
            if (d.isImage()) {
                //TODO prjects ??? ????????? ?????? ?????? ?????? ??? ???????????? ???...
                d.setRawRelativePath(String.format("/projects/%s/raw?path=%s", projectPath, d.getPath()));
            }
        });
    }


    /**
     *
     * @param projectPath
     * @param path
     * @param message
     * @return ????????? ???????????? ????????? Revision ??? ???????????????.
     */
    public long commitDeletion(String projectPath, String path, String message) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.isAdmin()) {
            ProjectRole projectRole = userService.getCurrentUserProjectRole(projectPath);
            if (projectRole == null || !projectRole.hasWriteRole()) {
                throw new InsufficientPrivilegeException();
            }
        }

        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, path);
        //TODO ??????????????? ????????? ??????
        logger.debug("???????????? '{}' ??? ?????? '{}' ??? ???????????????...", projectPath, repositoryUri);

        ISVNClient client = null;
        try {
            //Local Client ??? ??????????????? ???????????? ????????? ?????? ???????????? ????????? ??? ?????? ???????????? ????????????.
            //?????? ????????? ?????? ?????? ????????? ???????????? ???????????? ?????? ???????????? ????????? ????????? ?????? ????????? (????????? ?????? ?????? ????????? ?????? ?????? ????????? ???????????????
            //????????? HTTPD ???????????? ????????? ??????????????? ?????? ????????? ????????????).
            client = svnClientFactory.createLocalClient(currentUser.getName(), "");
            AtomicLong deletionRevision = new AtomicLong();
            client.remove(new HashSet<>(Arrays.asList(repositoryUri)), true, true, Collections.emptyMap(), (Set<CommitItem> set) -> message, (CommitInfo commitInfo) -> {
                logger.debug("Committed revision: {}", commitInfo.getRevision());
                deletionRevision.set(commitInfo.getRevision());
            });

            logger.info("???????????? '{}' ??? ?????? '{}' ??? ??????????????????. Revision '{}' ?????????.", projectPath, repositoryUri, deletionRevision.get());

            return deletionRevision.get();
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }
}
