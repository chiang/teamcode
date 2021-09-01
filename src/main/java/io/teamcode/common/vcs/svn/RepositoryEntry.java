package io.teamcode.common.vcs.svn;

import io.teamcode.common.IconsHelper;
import io.teamcode.common.unit.ByteSizeValue;
import io.teamcode.util.MimeTypeUtils;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Data
@Builder
@ToString
public class RepositoryEntry implements Comparable<RepositoryEntry> {
    private static final DateFormat dateFormatter = new SimpleDateFormat(
            "yyyy-MM-dd");

    private String groupPath;

    private String projectPath;

    private String path;

    private String name;

    private RepositoryEntryType type;

    private String mimeType;

    private long size;

    private String author;

    private String log;

    private Date lastModifiedAt;

    private String lastModifiedDate;

    private long lastChangedRevisionNumber;

    public String getIconClass() {
        return IconsHelper.fileTypeIconClass(type, name);
    }

    public String getByteSize() {
        return new ByteSizeValue(this.size).toString().toUpperCase();
    }

    //File Content 인 경우만
    public String getDownloadPath() {

        StringBuilder builder = new StringBuilder();
        builder.append("/");
        builder.append(this.groupPath);
        builder.append("/");
        builder.append(this.projectPath);
        builder.append("/raw?path=");
        builder.append(this.path);

        return builder.toString();
    }

    public String getDeletionPath() {
        StringBuilder builder = new StringBuilder();
        builder.append("/");
        builder.append(this.groupPath);
        builder.append("/");
        builder.append(this.projectPath);
        builder.append("/files?path=");
        builder.append(this.path);

        return builder.toString();
    }

    public String getDisplayName() {
        if (this.type == RepositoryEntryType.UP_LINK) {
            return "..";
        }
        else {
            return this.name;
        }
    }

    public boolean isRootEntry() {

        return this.path.equals("/") && StringUtils.isBlank(this.name) && this.getType() == RepositoryEntryType.DIRECTORY;
    }

    public boolean resolveUplink() {

        return this.getType() == RepositoryEntryType.DIRECTORY && StringUtils.isBlank(this.name);
    }

    public boolean isImage() {
        return MimeTypeUtils.isImage(this.mimeType);
    }

    /**
     * <p>앞의 저장소 주소를 제외한 나머지 절대 경로를 반환합니다.</p>
     * <p>만약, 현재 아이템 주소가 <code>http://svn.example.com/repos/petclinic/trunk/css</code> 라면, 이 메소드는 <code>trunk/css</code> 를 반환합니다.</p>
     *
     * @deprecated 그냥 Path 값을 쓰면 되지 않나?
     * @return
     */
    @Deprecated
    public String getAbsolutePath() {
        StringBuilder builder = new StringBuilder();
        if (this.path.equals("/")) {
            builder.append(this.name);
        } else {
            builder.append(this.path).append("/").append(this.name);
        }

        String absolutePath = builder.toString();
        if (absolutePath.startsWith("/")) {
            absolutePath = absolutePath.substring(1, absolutePath.length());
        }

        if (absolutePath.endsWith("/")) {
            absolutePath = StringUtils.removeEnd(absolutePath, "/");
        }

        return absolutePath;
    }

    public int compareTo(RepositoryEntry compareSourceEntry) {
        RepositoryEntryType compareSourceEntryType = compareSourceEntry.getType();

        if ((getType() == RepositoryEntryType.FILE)
                && (compareSourceEntryType == RepositoryEntryType.DIRECTORY)) {
            return 1;
        }
        if ((getType() == RepositoryEntryType.DIRECTORY)
                && (compareSourceEntryType == RepositoryEntryType.FILE)) {
            return -1;
        }

        return getName().compareTo(compareSourceEntry.getName());
    }
}
