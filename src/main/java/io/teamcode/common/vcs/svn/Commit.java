package io.teamcode.common.vcs.svn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.teamcode.domain.entity.User;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by chiang on 2017. 3. 19..
 */
@Data
public class Commit {

    public static final String COMMAND_TAG_SKIP_CI = "--skip-ci";

    private long revision;

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

    private Map<String, String> additionalRevisionProps = new HashMap<>();

    @JsonIgnore
    private transient List<ChangedFile> changedFiles = new ArrayList<ChangedFile>();

    /**
     * 이 커밋에 접근하기 위한 URL 경로
     */
    private transient String commitPath;

    /**
     * 시스템에 (데이터베이스에) 등록된 사용자 정보. 보통 이 정보가 없을 경우가 있습니다. 가져오기한 저장소인 경우 그럴 수 있습니다.
     */
    private transient User user;

    public Commit() {

    }

    public String getShortMessage() {
        if (StringUtils.hasText(message)) {
            return org.apache.commons.lang3.StringUtils.abbreviate(message, 20);
        }

        return "";
    }

    public void addRevisionProp(String propertyName, String propertyValue) {
        this.additionalRevisionProps.put(propertyName, propertyValue);
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
