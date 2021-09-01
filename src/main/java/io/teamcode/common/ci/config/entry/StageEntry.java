package io.teamcode.common.ci.config.entry;

import lombok.Getter;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class StageEntry {

    public static final String KEY = "stage";

    public static StageEntry DEFAULT_STAGE = new StageEntry("test");

    @Getter
    private String name;

    public StageEntry(String name) {
        this.name = name;
    }

}
