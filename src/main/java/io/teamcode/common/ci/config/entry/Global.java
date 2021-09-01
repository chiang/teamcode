package io.teamcode.common.ci.config.entry;

import io.teamcode.common.ci.config.Validatable;
import io.teamcode.common.ci.config.ValidationContext;
import lombok.Getter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class Global implements Validatable {

    private Map<String, String> keywords;

    private Map<String, Object> data;

    @Getter
    private StagesEntry stagesEntry;

    @Getter
    private List<JobEntry> jobEntries = new LinkedList<>();

    public Global(Map<String, String> keywords, Map<String, Object> data) {
        this.keywords = keywords;
        this.data = data;

        initialize(data);
    }

    //TODO 모든 항목을 중복 선언 체크 필요.
    private void initialize(Map<String, Object> data) {
        Iterator<String> keyIterator = data.keySet().iterator();
        String key;
        while(keyIterator.hasNext()) {
            key = keyIterator.next();
            if (keywords.containsKey(key)) {
                data.remove(key);
            }
            else {
                switch (key) {
                    case StagesEntry.KEY:
                        this.stagesEntry = new StagesEntry(data.remove(StagesEntry.KEY));
                        break;

                    case "before-script":
                        data.remove("before-script");
                        break;

                }
            }
        }

        if (this.stagesEntry == null)
            this.stagesEntry = new StagesEntry();

        for(String k: data.keySet()) {
            this.jobEntries.add(new JobEntry(this.stagesEntry, k, data.remove(k)));
        }
    }

    @Override
    public void validate(ValidationContext context) {

        validateJobEntries(context);
    }

    private void validateJobEntries(ValidationContext validationContext) {
        if (this.jobEntries.size() == 0)
            validationContext.addError("Job 이 최소한 한 개 이상 있어야 합니다.");
    }
}
