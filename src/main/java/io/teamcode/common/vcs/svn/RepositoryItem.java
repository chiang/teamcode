package io.teamcode.common.vcs.svn;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class RepositoryItem {

	private String path;

	private String name;

	private String author;

	private boolean directory;

	private Date lastModifiedAt;

	public RepositoryItem() {

	}

	public RepositoryItem(String path, String name, String author, boolean directory, Date lastModifiedAt) {
		this.path = path;
		this.name = name;
		this.author = author;
		this.directory = directory;
		this.lastModifiedAt = lastModifiedAt;
	}

}
