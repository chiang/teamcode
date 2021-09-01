package io.teamcode.common.vcs.svn;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 24..
 */
@Data
public class CommitDetails {

    private String author;

    private String message;

    private long revision;

    private Date createdAt;

    @Setter(AccessLevel.NONE)
    private List<Diff> diffs = new ArrayList<>();

    public void addDiffs(List<Diff> diffs) {
        this.diffs.addAll(diffs);
    }

    public int getTotalAdditions() {
        if (diffs != null)
            return diffs.stream().mapToInt(d -> d.getAdditions()).sum();

        return 0;
    }

    public int getTotalDeletions() {
        if (diffs != null)
            return diffs.stream().mapToInt(d -> d.getDeletions()).sum();

        return 0;
    }

    /**
     * trunk 인지, branches 인지, tags 인지 값이 들어갑니다. 값이 없다면 그냥 <code>null</code> 입니다.
     * FIXME 그럴리야 없겠지만, trunk 와 branches 를 한 번에 소스를 받아 두고 커밋할 수도 있지 않나?
     * TODO 표현하면 좋겠지만 지금은 방법이 없다.
     */
    public String getTree() {
        if (diffs == null || diffs.size() == 0) {
            return null;
        }
        else {
            /*if (diffs.stream().anyMatch(c -> c.getPath().startsWith("/trunk")))
                return "trunk";
            else if (diffs.stream().anyMatch(c -> c.getPath().startsWith("/branches")))
                return "branches";
            else if (diffs.stream().anyMatch(c -> c.getPath().startsWith("/tags")))
                return "tags";
            else*/
                return null;
        }
    }
}
