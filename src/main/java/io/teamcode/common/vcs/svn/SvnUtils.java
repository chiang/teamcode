package io.teamcode.common.vcs.svn;

import io.teamcode.common.vcs.SourceBrowserBreadcrumb;
import io.teamcode.common.Strings;
import io.teamcode.common.security.svn.Authz;
import io.teamcode.common.security.svn.AuthzFileGenerator;
import io.teamcode.util.DateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.subversion.javahl.types.Revision;
import org.apache.subversion.javahl.types.RevisionRange;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public abstract class SvnUtils {
	
	/*public static final String buildAbsoluteUrl(final String svnHost, final String repositoryName) {
		assert repositoryName != null;
		
		return buildAbsoluteUrl(svnHost, repositoryName);
	}*/

	public static final String buildAbsolutePath(final String repositoryRootDir, final String repositoryName, final String path) {
        assert repositoryRootDir != null;
        assert repositoryName != null;
        assert path != null;

        StringBuilder builder = new StringBuilder("file://");
        builder.append(repositoryRootDir);
        if (!repositoryRootDir.endsWith("/")) {
            builder.append("/");
        }
        builder.append(repositoryName);
        if (StringUtils.hasText(path)) {
            if (!path.startsWith("/")) {
                builder.append("/");
            }
            builder.append(path);
        }

        return builder.toString();
    }
	
	public static final String buildAbsoluteUrl(final String svnHost, final String repositoryName) {
		assert svnHost != null;
		assert repositoryName != null;
		
		StringBuilder builder = new StringBuilder();
		builder.append(svnHost);
		if (!svnHost.endsWith("/")) {
			builder.append("/");
		}
		builder.append(repositoryName);
		
		return builder.toString();
	}

    public static final String buildAbsoluteUrlAsFileScheme(final File repositoryDir) {
        StringBuilder builder = new StringBuilder();
        builder.append("file://");
        builder.append(repositoryDir.getAbsolutePath());

        return builder.toString();
    }

    public static final String buildAbsoluteUrlAsFileScheme(final String svnHost, final String repositoryName, final String path) {
        assert svnHost != null;
        assert repositoryName != null;

        StringBuilder builder = new StringBuilder();
        builder.append("file://");
        builder.append(buildAbsoluteUrl(svnHost, repositoryName, path));

        return builder.toString();
    }
	
	public static final String buildAbsoluteUrl(final String svnHost, final String repositoryName, final String path) {
		assert svnHost != null;
		assert repositoryName != null;
		
		StringBuilder builder = new StringBuilder();
		builder.append(svnHost);
		if (!svnHost.endsWith("/")) {
			builder.append("/");
		}
		builder.append(repositoryName);
		if (StringUtils.hasText(path)) {
			if (!path.startsWith("/")) {
				builder.append("/");
			}
			builder.append(path);
		}
		
		return builder.toString();
	}
	
	public static final void syncPasswordFile(final List<String> users, final File passwordFile) throws IOException {
		FileUtils.copyFile(passwordFile, new File(passwordFile.getParentFile(), passwordFile.getName() + String.format(".old.%s", DateUtils.defaultBackupSuffix())));
		FileUtils.writeLines(passwordFile, users);
	}
	
	public static final void syncAuthzFile(final Authz authz, final File authzFile) throws IOException {
		String newAuthz = new AuthzFileGenerator(authz).generate(true);
		FileUtils.copyFile(authzFile, new File(authzFile.getParentFile(), authzFile.getName() + String.format(".old.%s", DateUtils.defaultBackupSuffix())));
		FileUtils.writeStringToFile(authzFile, newAuthz, Charset.defaultCharset(), false);
	}

    public static final List<RevisionRange> revisionRanges(long startRevision) {
        List<RevisionRange> ranges = new ArrayList<>(1);
        ranges.add(new RevisionRange(Revision.getInstance(startRevision), Revision.getInstance(1)));

        return ranges;
    }

    public static final List<RevisionRange> headRevisionRanages() {
        List<RevisionRange> ranges = new ArrayList<RevisionRange>(1);
        ranges.add(new RevisionRange(Revision.HEAD, Revision.HEAD));

        return ranges;
    }

	public static final List<RevisionRange> oneRevisionRanages(long revision) {
		List<RevisionRange> ranges = new ArrayList<RevisionRange>(1);
		ranges.add(new RevisionRange(Revision.getInstance(revision), Revision.getInstance(revision)));

		return ranges;
	}

    public static final List<RevisionRange> oneRevisionRanages(Revision revision) {
        List<RevisionRange> ranges = new ArrayList<RevisionRange>(1);
        ranges.add(new RevisionRange(revision, revision));

        return ranges;
    }

    public static final List<RevisionRange> defaultRevisionRanges() {
        List<RevisionRange> ranges = new ArrayList<>(1);
        ranges.add(new RevisionRange(Revision.HEAD, Revision.getInstance(1)));

        return ranges;
    }

    public static final Set<String> defaultRevisionProps() {
        Set<String> revProps = new HashSet<String>();
        revProps.add("svn:log");
        revProps.add("svn:date");
        revProps.add("svn:author");

        return revProps;
    }

    public static final Set<String> defaultRevisionProps(Set<String> additionalRevisionProps) {
        Set<String> revProps = new HashSet<String>();
        revProps.add("svn:log");
        revProps.add("svn:date");
        revProps.add("svn:author");
        revProps.addAll(additionalRevisionProps);

        return revProps;
    }

    public static final Set<String> buildRevisionProps(Set<String> additionalRevisionProps) {
        Set<String> revProps = new HashSet<>();

        if (additionalRevisionProps != null)
            revProps.addAll(additionalRevisionProps);

        return revProps;
    }

    /**
     * trunk/src/main 과 같이 경로가 오면 아래 목록을 반환한다.
     *
     * [
     * "[contextpath]/repository/[repositoryName]/source?path='trunk'",
     * "[contextpath]/repository/[repositoryName]/source?path='trunk/src'",
     * "[contextpath]/repository/[repositoryName]/source?path='trunk/src/main'"
     * ]
     *
     * @return
     */
    public static List<SourceBrowserBreadcrumb> buildNavigationUrlString(String contextPath, final String groupPath, String projectPath, String path) {
        if (Strings.hasText(path)) {
            String[] paths = path.split("/");
            StringBuilder builder;
            List<SourceBrowserBreadcrumb> navs = new ArrayList<>();

            navs.add(new SourceBrowserBreadcrumb(projectPath, String.format("%s/%s/%s/files", contextPath, groupPath, projectPath), false));
            for (int i = 0; i < paths.length; i++) {
                builder = new StringBuilder(contextPath);
                builder.append("/").append(groupPath).append("/").append(projectPath).append("/files?path=");
                for (int j = i, k = 0; j >= 0; j--, k++) {
                    builder.append(paths[k]);
                    if (j > 0)
                        builder.append("/");
                }

                if (i == paths.length - 1)
                    navs.add(new SourceBrowserBreadcrumb(paths[i], builder.toString(), true));
                else
                    navs.add(new SourceBrowserBreadcrumb(paths[i], builder.toString(), false));
            }

            return navs;
        }
        else {
            return Arrays.asList(new SourceBrowserBreadcrumb(projectPath, String.format("%s/%s/%s/files", contextPath, groupPath, projectPath), true));
        }
    }

}
