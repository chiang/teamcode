package io.teamcode.common.ci.config.entry;

import io.teamcode.common.ci.config.Validatable;
import io.teamcode.common.ci.config.ValidationContext;
import io.teamcode.domain.entity.ci.JobWhen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * There are also two edge cases worth mentioning:

 If no stages are defined in .gitlab-ci.yml, then the build, test and deploy are allowed to be used as job's stage by default.
 If a job doesn't specify a stage, the job is assigned the test stage.
 */
public class StagesEntry implements Validatable {

    public static final String KEY = "stages";

    private Object data;

    private List<StageEntry> stageEntries = new ArrayList<>();

    public StagesEntry() {
        this.stageEntries.add(new StageEntry("build"));
        this.stageEntries.add(StageEntry.DEFAULT_STAGE);
        this.stageEntries.add(new StageEntry("deploy"));
    }

    public StagesEntry(Object data) {
        this.data = data;

        initialize((List<String>)data);
    }

    //TODO duplicate check
    private void initialize(List<String> data) {
        for (String s: data) {
            System.out.println("stage name: " + s);
            this.stageEntries.add(new StageEntry(s));
        }
    }

    @Override
    public void validate(ValidationContext context) {
        if (!(this.data instanceof List)) {
            context.addError(String.format("%s 설정에는 값이 배열 형태이어야 합니다.", KEY));
        }
    }

    public List<StageEntry> getStageEntries() {
        return this.stageEntries;
    }

}
