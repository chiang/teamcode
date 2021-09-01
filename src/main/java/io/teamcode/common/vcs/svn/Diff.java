package io.teamcode.common.vcs.svn;

import io.teamcode.common.IconsHelper;
import io.teamcode.common.Strings;
import io.teamcode.common.vcs.svn.diff.Hunk;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 25..
 */
@Data
public class Diff implements Comparable<Diff> {

    private boolean tooLarge = false;

    /**
     * 저장소 이름 이후부터 경로. 파일 명을 포함합니다.
     *
     */
    private String path;

    private String fromFileName;

    private String toFileName;

    private List<String> headerLines = new ArrayList<>();

    private List<Hunk> hunks = new ArrayList<>();

    private boolean cannotDisplay;

    private boolean image;

    private String mimeType;

    /**
     * Raw 를 조회하기 위힌 상태 경로. 앞 경로는 서버 URL 이 됩니다.
     */
    private String rawRelativePath;

    /**
     * 바이너리 파일의 경우 property changes 가 오는데 이 여부를 확인
     */
    private boolean propertyChanges;

    private int additions = 0;

    private int deletions = 0;

    public Diff() {

    }

    public Diff(String path, boolean cannotDisplay) {
        this.path = path;
        this.cannotDisplay = cannotDisplay;
    }

    public String getIconClass() {
        return IconsHelper.fileTypeIconClass(RepositoryEntryType.FILE, path);
    }

    public boolean isNotPropertyChangesOn() {
        return !this.propertyChanges;
    }

    public void plusAdditions() {
        this.additions++;
    }

    public void plusDeletions() {
        this.deletions++;
    }

    /**
     * Gets the last {@link org.wickedsource.diffparser.api.model.Hunk} of changes that is part of this Diff.
     *
     * @return the last {@link org.wickedsource.diffparser.api.model.Hunk} that has been added to this Diff.
     */
    public Hunk getLatestHunk() {
        if (!hunks.isEmpty())
            return hunks.get(hunks.size() - 1);
        else
            return null;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
        if(this.mimeType.startsWith("image"))
            this.image = true;
        else
            this.image = false;
    }

    public String getFileName() {

        return Strings.getFileNameFromPath(this.getPath());
    }

    public int compareTo(Diff anotherDiff) {

        return getPath().toLowerCase().compareTo(anotherDiff.getPath().toLowerCase());
    }

}
