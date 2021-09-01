package io.teamcode.common.vcs.svn;

import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SvnPathBuilder {
	
	@Autowired
	TcConfig tcConfig;
	
	public final String buildRootPath(Project project) {
		return SvnUtils.buildAbsoluteUrl(tcConfig.getRepositoryRootPath(), project.getPath());
	}
	
	public final String branchRootPath(Project project) {
		return SvnUtils.buildAbsoluteUrl(tcConfig.getRepositoryRootPath(), project.getPath(), "/branches");
	}
	
	public final String buildPath(Project project, String path) {
		return SvnUtils.buildAbsoluteUrl(tcConfig.getRepositoryRootPath(), project.getPath(), path);
	}
	
	public final String buildBranchPath(Project project, String path) {
		if (path.startsWith("/")) {
			return new StringBuilder(branchRootPath(project)).append(path).toString();
		}
		else {
			return new StringBuilder(branchRootPath(project)).append("/").append(path).toString();
		}
	}

}
