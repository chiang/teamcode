package io.teamcode.common.vcs.svn.callback;

import io.teamcode.common.vcs.svn.RepositoryItem;
import org.apache.subversion.javahl.callback.ListCallback;
import org.apache.subversion.javahl.types.DirEntry;
import org.apache.subversion.javahl.types.Lock;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SvnListCallback implements ListCallback {
	
	private List<RepositoryItem> repositoryItems = new ArrayList<>();

	@Override
	public void doEntry(DirEntry dirent, Lock lock) {
		
		//path 값이 비어 있으면 현재 디렉터리이다. svn list command는 현재 디렉터리 정보도 함께 보여주므로 이렇게 처리할 필요가 있다.
		if (StringUtils.hasText(dirent.getPath())) {
			switch(dirent.getNodeKind()) {
				case dir:
					repositoryItems.add(new RepositoryItem(dirent.getAbsPath(), dirent.getPath(), dirent.getLastAuthor(), true, dirent.getLastChanged()));
					break;
					
				default:
					repositoryItems.add(new RepositoryItem(dirent.getAbsPath(), dirent.getPath(), dirent.getLastAuthor(), false, dirent.getLastChanged()));
					break;
			}				
		}
	}
	
	public List<RepositoryItem> getRepositoryItems() {
		return this.repositoryItems;
	}

}
