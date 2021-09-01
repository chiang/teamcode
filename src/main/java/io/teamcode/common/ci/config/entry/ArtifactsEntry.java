package io.teamcode.common.ci.config.entry;

import io.teamcode.domain.entity.ci.ArtifactsWhen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Created by chiang on 2017. 6. 28..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtifactsEntry {

    public static final String KEY = "artifacts";

    public static final String KEY_NAME = "name";

    public static final String KEY_EXPIRE_IN = "expire-in";

    public static final String KEY_WHEN = "when";

    /**
     * Artifacts 이름 (파일 명).
     *
     * TODO 파일 명으로 사용할 수 있는 것만 허용해야 한다. 한글도 안 된다.
     *
     */
    private String name;

    private List<String> paths;

    private ArtifactsWhen when = ArtifactsWhen.ON_SUCCESS;

    /**
     *
     * <ul>
     *     <li>3 mins 4 sec</li>
     *     <li>2 hrs 20 min</li>
     *     <li>2h20min</li>
     *     <li>6 mos 1 day</li>
     *     <li>47 yrs 6 mos and 4d</li>
     *     <li>3 weeks and 2 days</li>
     * </ul>
     *
     */
    private String expireIn;

    public List<String> getPaths() {

        if (this.paths == null)
            return Collections.emptyList();
        else
            return this.paths;
    }

 }
