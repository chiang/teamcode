package io.teamcode.service.project.integration;

import io.teamcode.common.vcs.svn.ChangedFile;
import io.teamcode.common.vcs.svn.ChangedFileAction;
import io.teamcode.domain.entity.Project;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 커밋 시 발송하는 이메일에서 사용하는 정보들을 표현합니다.
 *
 */
@Data
public class EmailOnCommitObject {

    private Project project;

    private String subject;

    private String commitMessage;

    private String author;

    private String recipient;

    private String commitUrl;

    private int numberOfChangedFiles;

    private transient List<ChangedFile> changedFiles;

    /**
     * 변경된 파일의 변경 유형에 따른 건수를 보여줍니다. 건수는 파일 단위입니다.
     *
     * @return
     */
    public String getCommitDetails() {
        Assert.notNull(changedFiles);

        long deletions = changedFiles.stream().filter(c -> c.getAction() == ChangedFileAction.DELETE).count();
        long additions = changedFiles.stream().filter(c -> c.getAction() == ChangedFileAction.ADD).count();
        long modifications = changedFiles.stream().filter(c -> c.getAction() == ChangedFileAction.MODIFY).count();

        List<String> fragments = new ArrayList<>();
        if (additions > 0) {
            fragments.add(String.format("추가 %s 건", additions));
        }
        if (deletions > 0) {
            fragments.add(String.format("삭제 %s 건", deletions));
        }
        if (modifications > 0) {
            fragments.add(String.format("수정 %s 건", modifications));
        }
        //TODO replace?

        return StringUtils.join(fragments, ", ");
    }
}
