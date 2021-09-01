package io.teamcode.service.vcs.svn;

import io.teamcode.common.vcs.RepositoryHelper;
import io.teamcode.common.vcs.svn.RepositoryItem;
import io.teamcode.common.vcs.svn.SvnClientFactory;
import io.teamcode.common.vcs.svn.SvnPathBuilder;
import io.teamcode.common.vcs.svn.callback.SvnCommitCallback;
import io.teamcode.common.vcs.svn.callback.SvnCommitMessageCallback;
import io.teamcode.common.vcs.svn.callback.SvnListCallback;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.service.vcs.DuplicateRepositoryException;
import io.teamcode.service.vcs.SourceRepositoryCreationException;
import org.apache.subversion.javahl.ClientException;
import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.types.Depth;
import org.apache.subversion.javahl.types.DirEntry;
import org.apache.subversion.javahl.types.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SvnAdminService {

	private static final Logger logger = LoggerFactory.getLogger(SvnAdminService.class);
	
	@Autowired
	TcConfig tcConfig;
	
	@Autowired
	private SvnClientFactory svnClientFactory;
	
	@Autowired
	private SvnPathBuilder svnPathBuilder;

	public String[] findAllRepositoryNames() {
		File[] dirsOrFiles = this.tcConfig.getRepositoryRootDir().listFiles();
		
		List<String> repositories = new ArrayList<String>();
		for (File dirOrFile: dirsOrFiles) {
			if (dirOrFile.isDirectory() && !dirOrFile.isHidden()) {
				repositories.add(dirOrFile.getName());
			}
		}
		return repositories.toArray(new String[]{});
	}

	public boolean exist(String repositoryName) {
		File repositoryRootDir = new File(this.tcConfig.getRepositoryRootDir(), repositoryName);
		return repositoryRootDir.exists();
	}
	
	public void create(Project project, boolean isDefaultLayout)
			throws SourceRepositoryCreationException {
		//ensureValidProject(repository);

		//SourceRepository sourceRepository = repository.getSourceRepository();
		File repositoryDir = new File(this.tcConfig.getRepositoryRootDir(),
                project.getName());
		if (repositoryDir.exists()) {
			throw new DuplicateRepositoryException(project.getName());
		}
        //SvnRepositoryHandler.createRepositoryFromTemplate(repositoryDir);
		this.setRepositoryPermission(repositoryDir);

		if (isDefaultLayout) {
			logger.info("기본 레이아웃을 생성하도록 설정되었습니다. Repository 기본 레이아웃을 생성합니다...");

			try {
				buildDefaultRepositoryLayout(project);
			} catch (ClientException e) {
				//TODO 사용자에게 알려줘야 한다.
				logger.error("cannot build default repository layout. skip building default layout.", e);
			}
		}
		else {
			logger.info("기본 레이아웃으로 생성하지 않고 완료합니다.");
		}
	}
	
	private void setRepositoryPermission(File repositoryDir) {
		File repositoryDbDir = new File(repositoryDir, "db");
		
		//repositoryDbDir.setWritable(true, false);
		repositoryDbDir.setWritable(true, true);
		
		File[] repositoryDbFiles = repositoryDbDir.listFiles();
		for (File f: repositoryDbFiles) {
			//f.setWritable(true, false);
			f.setWritable(true, true);
		}
	}
	
	private void buildDefaultRepositoryLayout(Project project) throws ClientException {
		String url = svnPathBuilder.buildRootPath(project);
		ISVNClient client = svnClientFactory.createLocalClient();
		SvnListCallback callback = new SvnListCallback();
		client.list(url, Revision.HEAD, null, Depth.immediates, DirEntry.Fields.all, true, callback);
		
		List<RepositoryItem> repositoryItems = callback.getRepositoryItems();
		boolean existTrunk = false;
		boolean existBranches = false;
		boolean existTags = false;
		for (RepositoryItem file: repositoryItems) {
			if (file.getName().toLowerCase().equals("trunk")) {
				existTrunk = true;
			}
			else if (file.getName().toLowerCase().equals("branches")) {
				existBranches = true;
			}
			else if (file.getName().toLowerCase().equals("tags")) {
				existTags = true;
			}
		}
		
		Set<String> directoryNames = new HashSet<String>();
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
	}
	
	private void mkdirToRepository(ISVNClient client, Set<String> paths) throws ClientException {
		
		logger.debug("make directory to remote repository: {}", paths);
		
		SvnCommitMessageCallback handler = new SvnCommitMessageCallback("created default repository layout.");//이거 없으면 생성 안 됨.
		SvnCommitCallback callback = new SvnCommitCallback();
		
		client.mkdir(paths, false, null, handler, null);
	}



	private void ensureValidProject(Project project) {
		if ((project == null)
				|| (!StringUtils.hasText(project.getPath()))
            //|| (!repository.getSourceRepository().getType().equals(SourceRepositoryType.SUBVERSION))
				)
			throw new SourceRepositoryCreationException(
					"Repository information is not valid. Repository object is a null or type is not subversion");
	}
}