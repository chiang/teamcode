package io.teamcode.common.vcs.svn;

import io.teamcode.common.Strings;
import io.teamcode.util.FileSystemUtils;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class ChangedFile implements Comparable<ChangedFile> {

	private ChangedFileAction action;

    /**
     * 전체 경로 (저장소 URL 제외한, trunk 부터). 파일 이름도 포함.
     */
	private String path;

	//dir or file
	private String nodeKind;

    private boolean image;
	
	public ChangedFile(ChangedFileAction action, String path) {
		this.action = action;
		this.path = path;
	}
	
	public ChangedFile(ChangedFileAction action, String path, String nodeKind) {
		this.action = action;
		this.path = path;
		this.nodeKind = nodeKind;

        String mimeType = FileSystemUtils.detectMimeType(this.path);
        if (StringUtils.hasText(mimeType)) {
            if(mimeType.startsWith("image"))
                this.image = true;
        }
	}

    public String getName() {

        return Strings.getFileNameFromPath(this.getPath());
    }

	public int compareTo(ChangedFile changedFile) {

		return getPath().toLowerCase().compareTo(changedFile.getPath().toLowerCase());
	}

}
