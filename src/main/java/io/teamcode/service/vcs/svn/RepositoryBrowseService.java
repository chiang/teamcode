package io.teamcode.service.vcs.svn;

import io.teamcode.common.Strings;
import io.teamcode.common.converter.MarkdownConverter;
import io.teamcode.common.converter.ReadmeConversionException;
import io.teamcode.common.io.KnownFilesResolver;
import io.teamcode.common.vcs.RepositoryBrowseContent;
import io.teamcode.common.vcs.SourceBrowserBreadcrumb;
import io.teamcode.common.vcs.VcsCommunicationFailureException;
import io.teamcode.common.vcs.svn.*;
import io.teamcode.config.TcConfig;
import io.teamcode.util.FileSystemUtils;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.callback.ListCallback;
import org.apache.subversion.javahl.callback.LogMessageCallback;
import org.apache.subversion.javahl.types.*;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Component
public class RepositoryBrowseService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryBrowseService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    KnownFilesResolver knownFilesResolver;

    @Autowired
    SvnClientFactory svnClientFactory;

    public RepositoryBrowseContent getRepositoryBrowseContent(final String contextPath, final String groupPath, final String projectPath, final String path, final long revisionNumber) {
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), projectPath, path);

        RepositoryBrowseContent repositoryBrowseContent = new RepositoryBrowseContent(knownFilesResolver);
        repositoryBrowseContent.setRevision(revisionNumber);
        repositoryBrowseContent.setBreadcrumbs(SvnUtils.buildNavigationUrlString(contextPath, "projects", projectPath, path));

        List<RepositoryEntry> entries = findEntries(groupPath, projectPath, path, revisionNumber);
        repositoryBrowseContent.setEntries(entries);

        if (logger.isDebugEnabled()) {
            logger.debug("resolved repository entries:");
            for (RepositoryEntry entry: entries) {
                logger.debug("\tentry: {}", entry);
            }
        }

        if (repositoryBrowseContent.isFileContent()) {
            //File content ??? ??????, http://.../abc.txt ??? ?????? svn list ??? ????????? ????????????, ??? ?????? ?????? ????????? ????????? ????????? (DirEntry.getPath() ?????? ?????? ??????).
            //????????? ????????? ????????? ?????? ??????.
            repositoryBrowseContent.getOneEntryIfFileContent().setName(Strings.getFileNameFromPath(repositoryBrowseContent.getOneEntryIfFileContent().getPath()));
        }
        else {
            //repositoryBrowseContent.getOneEntryIfDirectoryContent().setName("..");

            for (SourceBrowserBreadcrumb breadcrumb: repositoryBrowseContent.getBreadcrumbs()) {
                logger.debug("bread --> {}", breadcrumb);
            }

            if (repositoryBrowseContent.getBreadcrumbs().size() >= 2) {
                repositoryBrowseContent.getEntries().stream()
                        .filter(e -> e.getType() == RepositoryEntryType.UP_LINK)
                        .forEach(e -> e.setPath(repositoryBrowseContent.getBreadcrumbs().get(repositoryBrowseContent.getBreadcrumbs().size() - 2).getPath()));
            }

            //repositoryBrowseContent.getOneEntryIfDirectoryContent().setPath(repositoryBrowseContent.getBreadcrumbs().get(repositoryBrowseContent.getBreadcrumbs().size() - 2).getPath());
            //repositoryBrowseContent.getOneEntryIfDirectoryContent().setType(RepositoryEntryType.UP_LINK);
        }

        if (repositoryBrowseContent.isDisplayableIfFileContent()) {
            if (repositoryBrowseContent.getOneEntryIfFileContent().isImage()) {
                logger.debug("This content is a image type.");
            }
            else {
                //TODO check size 10MB?
                byte[] raw = populateContent(repositoryUri, revisionNumber);

                if (MarkdownConverter.isMarkdownFile(repositoryBrowseContent.getOneEntryIfFileContent().getName())) {
                    try {
                        //TODO ?????? ???????????? ???????????? Detection ?????? ???????????? ???..
                        repositoryBrowseContent.setContent(MarkdownConverter.convert(new String(raw, "UTF-8")), true);
                    } catch (ReadmeConversionException e) {
                        //TODO ?????? Plain ?????? ????????? ?????? ???????
                        repositoryBrowseContent.setContent("????????? ???????????? ??? ????????? ?????? ??? ????????????.", false);//FIXME ????????????.
                    } catch (UnsupportedEncodingException e) {
                        repositoryBrowseContent.setContent("????????? ????????? ??? ????????? ?????? ??? ????????????.", false);//FIXME ????????????.
                    }
                }
                else {
                    try {
                        //FIXME html ??? ?????? ??????????????? charset ??? ????????? ?????? ????????? ????????? ?????? ?????? ?????????!!!
                        UniversalDetector detector = new UniversalDetector(null);
                        detector.handleData(raw, 0, raw.length);
                        detector.dataEnd();
                        String detectedCharset = detector.getDetectedCharset();
                        logger.debug("code page detected. charset: {}", detectedCharset);

                        if (StringUtils.hasText(detectedCharset))
                            repositoryBrowseContent.setContent(HtmlUtils.htmlEscape(new String(raw, detectedCharset)), false);
                        else
                            repositoryBrowseContent.setContent(HtmlUtils.htmlEscape(new String(raw, "UTF-8")), false);

                    } catch (UnsupportedEncodingException e) {
                        logger.error(e.getMessage(), e);
                        repositoryBrowseContent.setContent("????????? ????????? ??? ????????? ?????? ??? ????????????.", false);//FIXME ????????????.
                    }
                }
            }
        }

        return repositoryBrowseContent;
    }

    /**
     * ???????????? Subversion Repository ?????? ?????? ????????? ???????????? ???????????? ????????? ????????? ????????? ???????????????.
     *
     * @param repositoryUri <code>http://svn.example.com/repos/bootstrap/trunk/README.txt</code> ??? ?????? ??????
     * @return
     */
    private byte[] populateContent(final String repositoryUri, final long revisionNumber) {
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient();
            Revision revision = revisionNumber <= -1 ? Revision.HEAD : Revision.getInstance(revisionNumber);
            byte[] raw = client.fileContent(repositoryUri, revision, revision);

            return raw;
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    /**
     * ???????????? Subversion Repository ?????? ?????? ????????? ???????????? ???????????? OutputStream ?????? ?????????.
     *
     * @param repositoryUri <code>http://svn.example.com/repos/bootstrap/trunk/README.txt</code> ??? ?????? ??????
     * @return
     */
    public void populateContentAsStream(final String repositoryUri, final OutputStream outputStream) {
        logger.debug("????????? raw ????????? ?????????????????????. ????????? ???????????????... (URI: '{}')", repositoryUri);
        ISVNClient client = null;
        try {
            client = svnClientFactory.createLocalClient();
            client.streamFileContent(repositoryUri, Revision.HEAD, Revision.HEAD, outputStream);
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);

            throw new VcsCommunicationFailureException(e.getMessage(), e);
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    /**
     *
     * @param projectPath
     * @param path ?????? <code>null</code> ??????
     * @return
     */
    public List<RepositoryEntry> findEntries(String groupPath, String projectPath, String path, final long revisionNumber) {
        //ensureProjectMemberOrSuperAdmin();

        if (StringUtils.hasText(path)) {
            return findEntriesForPath(groupPath, projectPath, path, revisionNumber);
        }

        return findEntriesForPath(groupPath, projectPath, "", revisionNumber);
    }

    private List<RepositoryEntry> findEntriesForPath(final String groupPath, final String projectPath, String path, final long revisionNumber) {
        ISVNClient client = svnClientFactory.createLocalClient();

        //TODO
        String newPath = null;

        //if (StringUtils.hasText(repository.getStartRepositoryPath())) {
        //    logger.debug("????????? '{}' ??? ?????? ????????? ????????? ??? '{}' ??? ?????? ???????????? ???????????? ??????????????????.", repository.getName(), repository.getStartRepositoryPath());
        //}

        SourceListCallback callback = new SourceListCallback(client, projectPath);
        try {
            String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), projectPath, path);

            if (logger.isDebugEnabled()) {
                logger.debug("Repository '{}' ??? ?????? '{}' ?????? 'svn list' ???????????? ????????? ???????????????...", projectPath, repositoryUri);
                logger.debug("????????? ?????? ??????: {} with r{}", path, revisionNumber);
            }

            Revision revision = revisionNumber == -1 ? Revision.HEAD : Revision.getInstance(revisionNumber);
            Revision pegRevision = null;
            int direntFields = -1;
            boolean fetchLocks = false;
            client.list(repositoryUri, revision, pegRevision, Depth.immediates, direntFields, fetchLocks, callback);
        } catch (ClientException e) {
            SvnExceptionHandler.throwDetailSvnException(e);
        }

        List<RepositoryEntry> results = callback.getSourceEntries();
        results.stream().forEach(e -> {
            e.setGroupPath(groupPath);
            e.setProjectPath(projectPath);
        });

        if (logger.isDebugEnabled()) {
            for (RepositoryEntry se: results) {
                logger.debug("repository entries: {}", se);
            }
        }
		/*if ((callback.isFile()) && (results.size() == 0)) {
			return getRawContent(repository.getName(), path, false);
		}*/

        logger.debug("??? '{}'?????? ?????? ????????? ??????????????????. ????????? ???????????????...", results.size());

        if (results.size() > 0) {
            //?????? ???????????? ??? ??????.
            RepositoryEntry repositoryEntry = null;
            if (results.stream().anyMatch(r -> r.resolveUplink() && !r.isRootEntry())) {
                repositoryEntry = results.stream().filter(r -> r.resolveUplink() && !r.isRootEntry()).findFirst().get();
                repositoryEntry.setType(RepositoryEntryType.UP_LINK);
            }

            Collections.sort(results);
            List<RepositoryEntry> sortedEntries = new ArrayList<>();
            if (repositoryEntry != null)
                sortedEntries.add(repositoryEntry);

            results.stream().filter(r -> r.getType() != RepositoryEntryType.UP_LINK && !r.isRootEntry()).forEach(r -> sortedEntries.add(r));

            return sortedEntries;
        }
        else {
            return results;
        }
    }

    class SourceListCallback implements ListCallback {
        private ISVNClient client;

        private String repositoryName;

        private String parentPath;

        private List<RepositoryEntry> dirEntries = new ArrayList<>();
        private boolean file = false;
        private SvnLogMessageCallback logCallback = new SvnLogMessageCallback();

        public SourceListCallback(ISVNClient client, final String repositoryName) {
            this.client = client;
            this.repositoryName = repositoryName;
        }

        @Override
        public void doEntry(DirEntry dirEntry, Lock lock) {
            logger.debug("doEntry --> path: {}, absolute path: {}", dirEntry.getPath(), dirEntry.getAbsPath());
            this.dirEntries.add(buildSourceEntry(dirEntry));
        }

        private final RepositoryEntry buildSourceEntry(DirEntry dirEntry) {
            //dirEntry ?????? getPath ??? ?????? ????????? ???????????? ?????????. ????????? Strings.getFileNameFromPath ??? ???????????? ?????? ????????? ????????? ??? ????????????.
            logger.debug("file path: {}, absolute: {}, node kind: {}", dirEntry.getPath(), dirEntry.getAbsPath(), dirEntry.getNodeKind());
            RepositoryEntry entry = RepositoryEntry.builder()
                    .path(dirEntry.getAbsPath())
                    .name(dirEntry.getPath())
                    .type(dirEntry.getNodeKind() == NodeKind.dir ? RepositoryEntryType.DIRECTORY : RepositoryEntryType.FILE)
                    .size(dirEntry.getSize())
                    .author(dirEntry.getLastAuthor())
                    .lastModifiedAt(dirEntry.getLastChanged())
                    .lastChangedRevisionNumber(dirEntry.getLastChangedRevisionNumber()).build();

            String detectedMimeType = FileSystemUtils.detectMimeType(entry.getPath());
            entry.setMimeType(detectedMimeType);

            //TODO ???????????? ?????? ?????? ????????? ?????? ????????? ?????????!!! ???????????? ????????????!!!
            try {
                String repositoryItemUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), this.repositoryName, entry.getAbsolutePath());
                this.client
                        .logMessages(repositoryItemUri,
                                null, SvnUtils.headRevisionRanages(), false, true, false,
                                SvnUtils.defaultRevisionProps(), 0L,
                                this.logCallback);
                entry.setLog(this.logCallback.getMessage());

                return entry;
            } catch (ClientException e) {
                //FIXME defail log...
                RepositoryBrowseService.logger.error("cannot retrieve commit log.", e);

                return entry;
            }
        }

        public List<RepositoryEntry> getSourceEntries() {
            return this.dirEntries;
        }

        public void clear() {
            this.dirEntries.clear();
        }

        public boolean isFile() {
            return this.file;
        }
    }

    class SvnLogMessageCallback implements LogMessageCallback {
        private String message;

        SvnLogMessageCallback() {
        }

        public void singleMessage(Set<ChangePath> changedPaths, long revision,
                                  Map<String, byte[]> revprops, boolean hasChildren) {
            try {
                this.message = new String(revprops.get("svn:log"),
                        "UTF8");
            } catch (UnsupportedEncodingException e) {
                this.message = new String(revprops.get("svn:log"));
            }
        }

        public String getMessage() {
            return this.message;
        }
    }
}
