package io.teamcode.common.vcs.svn;

import io.teamcode.common.io.TeamcodePermissionHelper;
import io.teamcode.common.vcs.RepositoryHelper;
import io.teamcode.common.vcs.svn.callback.SvnCommitMessageCallback;
import io.teamcode.common.vcs.svn.callback.SvnListCallback;
import io.teamcode.service.vcs.SourceRepositoryCreationException;
import io.teamcode.util.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.types.Depth;
import org.apache.subversion.javahl.types.DirEntry;
import org.apache.subversion.javahl.types.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SvnRepositoryHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SvnRepositoryHandler.class);

	//javahl 1.8 에서는 동작하지 않음
    /*public static void createRepository(final String endpoint, File tcHomeDir, File repositoryDirectory) {
        SVNAdmin svnAdmin = new SVNAdmin();
        try {
            //FIXME tmp에 대해서 정확히 이해하기
            svnAdmin.create(repositoryDirectory.getAbsolutePath(), false, false, "/tmp", SVNAdmin.FSFS);

            boolean isDefaultLayout = true;
            if (isDefaultLayout) {
                logger.info("기본 레이아웃을 생성하도록 설정되었습니다. 서브버전 기본 레이아웃을 생성합니다...");

                try {
                    buildDefaultRepositoryLayout(repositoryDirectory);
                } catch (org.apache.subversion.javahl.ClientException e) {
                    //TODO 사용자에게 알려줘야 한다.
                    logger.error("cannot build default repository layout. skip building default layout.", e);
                }
            }
            else {
                logger.info("기본 레이아웃으로 생성하지 않고 완료합니다.");
            }

            String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(endpoint, repositoryDirectory.getName(), tcHomeDir.getAbsolutePath());

            logger.debug("'{}' Repository 의 Hook Script 를 검증합니다.", repositoryDirectory.getName());
            logger.debug("'{}' Repository 의 Post Commit Hook URL: <{}>", repositoryDirectory.getName(), repositoryPostCommitHookUrl);

            RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl);
        } catch (ClientException e) {
                throw new SourceRepositoryCreationException("Cannot create repository directory '" + repositoryDirectory.getAbsolutePath() + "'", e);
        } finally {
                svnAdmin.dispose();
        }
    }*/

	public static final void createRepositoryFromTemplate(final String version, final String endpoint, File tcHomeDir, File repositoryDirectory, boolean isSyncPermission) throws IOException {
	    if ("1.8".equals(version)) {
            InputStream inputStream = SvnRepositoryHandler.class.getResourceAsStream("/subversion-repository-template-1.8.zip");
            FileSystemUtils.unzip(inputStream, repositoryDirectory);
        }
        else {
            InputStream inputStream = SvnRepositoryHandler.class.getResourceAsStream("/subversion-repository-template-1.9.zip");
            FileSystemUtils.unzip(inputStream, repositoryDirectory);

            TeamcodePermissionHelper.validateRepositoryDirectoryThenSetOwner(repositoryDirectory);
        }

        boolean isDefaultLayout = true;
        if (isDefaultLayout) {
            logger.info("기본 레이아웃을 생성하도록 설정되었습니다. 서브버전 기본 레이아웃을 생성합니다...");

            try {
                buildDefaultRepositoryLayout(repositoryDirectory);
            } catch (org.apache.subversion.javahl.ClientException e) {
                //TODO 사용자에게 알려줘야 한다.
                logger.error("cannot build default repository layout. skip building default layout.", e);
            }
        }
        else {
            logger.info("기본 레이아웃으로 생성하지 않고 완료합니다.");
        }

        String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(endpoint, repositoryDirectory.getName(), tcHomeDir.getAbsolutePath());

        logger.debug("'{}' Repository 의 Hook Script 를 검증합니다.", repositoryDirectory.getName());
        logger.debug("'{}' Repository 의 Post Commit Hook URL: <{}>", repositoryDirectory.getName(), repositoryPostCommitHookUrl);

        RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl, isSyncPermission);
	}

    public static final void archive(File repositoryDir, File archivedRepositoriesRootDir) throws IOException {
        logger.info("저장소 '{}' 를 아키이브합니다...", repositoryDir.getName());

        if (repositoryDir.exists()) {
            File archivedDir = new File(archivedRepositoriesRootDir, repositoryDir.getName());
            FileUtils.moveDirectory(repositoryDir, archivedDir);
        }
        else {
            logger.warn("저장소 '{}' 가 존재하지 않아 아키이브 요청 처리를 건너뜁니다.", repositoryDir.getName());
        }
    }

    private static final void buildDefaultRepositoryLayout(final File repositoryDir) throws org.apache.subversion.javahl.ClientException {
        String url = SvnUtils.buildAbsoluteUrlAsFileScheme(repositoryDir);
        ISVNClient client = null;
        try {
            client = new SvnClientFactory().createLocalClient();
            SvnListCallback callback = new SvnListCallback();
            client.list(url, Revision.HEAD, null, Depth.immediates, DirEntry.Fields.all, true, callback);

            List<RepositoryItem> repositoryItems = callback.getRepositoryItems();
            boolean existTrunk = false;
            boolean existBranches = false;
            boolean existTags = false;
            for (RepositoryItem item : repositoryItems) {
                if (item.getName().toLowerCase().equals("trunk")) {
                    existTrunk = true;
                } else if (item.getName().toLowerCase().equals("branches")) {
                    existBranches = true;
                } else if (item.getName().toLowerCase().equals("tags")) {
                    existTags = true;
                }
            }

            Set<String> directoryNames = new HashSet<>();
            if (!url.endsWith("/")) {
                url += "/";
            }
            if (!existTrunk) {
                directoryNames.add(url + "trunk");
            }
            if (!existBranches) {
                directoryNames.add(url + "branches");
            }
            if (!existTags) {
                directoryNames.add(url + "tags");
            }

            mkdirToRepository(client, directoryNames);
        } finally {
            if (client != null)
                client.dispose();
        }
    }

    private static final void mkdirToRepository(ISVNClient client, Set<String> paths) throws org.apache.subversion.javahl.ClientException {
        logger.debug("make directory to remote repository: {}", paths);

        //TODO i18n?
        SvnCommitMessageCallback handler = new SvnCommitMessageCallback("기본 디렉터리를 생성했습니다.");//이거 없으면 생성 안 됨.

        client.mkdir(paths, false, null, handler, null);
    }

}
