package io.teamcode.common.vcs.svn;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * git나 mercurial, svn을 모두 같이 표현할 수 있다. 정말?
 * 
 * @author chiang
 * 
 */
@Data
public class ChangeSet {

	public static final String COMMAND_TAG_SKIP_CI = "--skip-ci";

	private String revision;

	private String message;

	private String author;

	private int additions = 0;

	private int deletions = 0;

	/**
	 * The item was replaced by a different one at the same location.
	 */
	private int modifications = 0;

	/**
	 * Properties or textual contents on the item were changed.
	 */
	private int replacements = 0;

    /**
     * 만약 이 파일이 다른 파일에서 Copy 된 것이거나 Move 된 것일 때 어디에서 왔는지를 알려줍니다. 이 값은 그래서 비어있을 수 있습니다. copySrcPath에 대응합니다.
     *
     */
    private String copiedFromPath;

    /**
     * <code>copiedFromPath</code> 값이 있을 때만 존재합니다. 원본 Revision 을 의미합니다.
     *
     */
    private long copiedFromRevision;

	private Date createdAt;

	private transient List<ChangedFile> changedFiles = new ArrayList<ChangedFile>();

	public ChangeSet() {

	}

	public ChangeSet(String revision, String message, String author, Date date) {
		this.revision = revision;
		this.message = message;
		this.author = author;
		this.createdAt = date;
	}

	public boolean isSkipCi() {
		if (StringUtils.hasText(this.getMessage())) {
			return this.getMessage().trim().startsWith(COMMAND_TAG_SKIP_CI);
		}

		return false;
	}

	public void plusAddtions() {
		this.additions++;
	}

	public void plusDeletions() {
		this.deletions++;
	}

	public void plusModifications() {
		this.modifications++;
	}

	public void plusReplacements() {
		this.replacements++;
	}

	public void addChangedFile(ChangedFile changedFile) {
		this.changedFiles.add(changedFile);
	}
}
