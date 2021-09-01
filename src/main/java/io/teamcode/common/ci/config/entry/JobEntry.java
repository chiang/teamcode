package io.teamcode.common.ci.config.entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamcode.common.ci.config.Validatable;
import io.teamcode.common.ci.config.ValidationContext;
import io.teamcode.common.ci.config.YamlValidator;
import io.teamcode.domain.entity.ci.ArtifactsWhen;
import io.teamcode.domain.entity.ci.JobWhen;
import io.teamcode.web.api.model.ci.Variable;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Data
public class JobEntry implements Validatable {

    private static final List<String> ALLOWED_KEYS = new ArrayList<>();

    public static final String KEY_VARIABLES = "variables";

    static {
        ALLOWED_KEYS.add("tags");
        ALLOWED_KEYS.add("script");
        ALLOWED_KEYS.add("only");
        ALLOWED_KEYS.add("except");
        ALLOWED_KEYS.add("type");
        ALLOWED_KEYS.add("image");
        ALLOWED_KEYS.add("services");
        ALLOWED_KEYS.add("allow_failure");
        ALLOWED_KEYS.add("type");
        ALLOWED_KEYS.add("stage");
        ALLOWED_KEYS.add("when");
        ALLOWED_KEYS.add("artifactsFile");
        ALLOWED_KEYS.add("cache");
        ALLOWED_KEYS.add("dependencies");
        ALLOWED_KEYS.add("before_script");
        ALLOWED_KEYS.add("after_script");
        ALLOWED_KEYS.add(KEY_VARIABLES);
        ALLOWED_KEYS.add("environment");
        ALLOWED_KEYS.add("coverage");
    }

    private static final List<String> WHEN_POSSIBLE_VALUES
            = Arrays.asList("on_success", "on_failure", "always", "manual");

    private Object data;

    private Map<String, Object> configData;

    private String name;

    //default is test
    private StageEntry stageEntry;

    private ArtifactsEntry artifactsEntry;

    private JobWhen when;

    private Script beforeScript;

    private Script script;

    private List<Variable> variables;

    public JobEntry(final StagesEntry stagesEntry, String name, Object data) {
        this.name = name;
        if (!(data instanceof Map)) {
            throw new ValidationError(String.format("JobEntry 설정 포멧이 잘못되었습니다. 속성 이름: '%s'", name));
        }

        this.configData = (Map)data;
        initialize(stagesEntry, configData);
    }

    private void initialize(final StagesEntry stagesEntry, Map<String, Object> data) {
        Iterator<String> keyIterator = data.keySet().iterator();
        String key;
        while (keyIterator.hasNext()) {
            key = keyIterator.next();
            switch(key) {
                case "when":
                    this.when = JobWhen.valueOf(((String)data.get("when")).toUpperCase());
                    break;

                case ArtifactsEntry.KEY:
                    this.artifactsEntry = buildArtifactsEntry((Map<String, Object>)data.get(ArtifactsEntry.KEY));
                    break;

                case StageEntry.KEY:
                    List<StageEntry> stageEntries = stagesEntry.getStageEntries().stream().filter(s -> s.getName().equals(data.get(StageEntry.KEY))).collect(Collectors.toList());
                    if (stageEntries.size() == 0)
                        this.stageEntry = StageEntry.DEFAULT_STAGE;
                    else
                        this.stageEntry = stageEntries.get(0);//FIXME 여러 개일리는 없으나...
                    break;

                case Script.KEY:
                    this.script = new Script(data.get(Script.KEY));
                    break;

                case KEY_VARIABLES:
                    buildJobVariables((Map<String, Object>)data.get(KEY_VARIABLES));
                    break;

                default:
                    break;
            }
        }


    }

    @Override
    public void validate(ValidationContext context) {



        //context.addError("JobEntry config should contain at least one visible job");

        YamlValidator.presence(context,null, "script");

        validateWhen(context, null);
    }

    private void validateWhen(ValidationContext context, Map<String, Object> jobConfig) {
        if (jobConfig.containsKey("when")) {
            String value = (String)jobConfig.get("when");
            if (StringUtils.hasText(value)) {
                if (!WHEN_POSSIBLE_VALUES.contains(value)) {
                    context.addError("should be on_success, on_failure, always or manual");
                }
            }
            else {
                context.addError("should be on_success, on_failure, always or manual");
            }
        }
    }

    //TODO validate
    public static final ArtifactsEntry buildArtifactsEntry(Map<String, Object> data) {
        List<String> paths = (List<String>)data.get("paths");

        ArtifactsEntry artifactsEntry = new ArtifactsEntry();
        if (data.containsKey(ArtifactsEntry.KEY_NAME)) {
            artifactsEntry.setName((String)data.get(ArtifactsEntry.KEY_NAME));
        }

        artifactsEntry.setPaths(paths);
        if (data.containsKey(ArtifactsEntry.KEY_EXPIRE_IN))
            artifactsEntry.setExpireIn((String)data.get(ArtifactsEntry.KEY_EXPIRE_IN));

        if (data.containsKey(ArtifactsEntry.KEY_WHEN)) {
            artifactsEntry.setWhen(ArtifactsWhen.valueOf(((String)data.get(ArtifactsEntry.KEY_WHEN)).toUpperCase()));
        }

        return artifactsEntry;
    }

    /**
     * 기본 Stage 는 test 입니다.
     *
     * @return
     */
    public StageEntry getStageEntry() {
        if (this.stageEntry == null)
            return StageEntry.DEFAULT_STAGE;

        return this.stageEntry;
    }

    public JobWhen getWhen() {
        if (this.when == null)
            return JobWhen.ON_SUCCESS;
        else
            return this.when;
    }

    /**
     * Before 와 Script 를 모두 합쳐서 줄바꿈을 구분자로 한 문자열로 반환합니다.
     *
     * @return
     */
    public String getCommands() {
        List<String> scripts = new ArrayList<>();
        if (this.getBeforeScript() != null && this.getBeforeScript().getCommands().size() > 0)
            scripts.addAll(this.getBeforeScript().getCommands());

        if (this.getScript() != null && this.getScript().getCommands().size() > 0)
            scripts.addAll(this.getScript().getCommands());

        return String.join("\n", scripts);
    }

    private void buildJobVariables(Map<String, Object> variablesMap) {
        if (variablesMap != null && !variablesMap.isEmpty()) {
            this.variables = new ArrayList<>();
            for (String name: variablesMap.keySet()) {
                //Object 값에는 yes, no 형태면 boolean 과 같이 올 수 있다. 그래서 String 으로 Casting 하지 않는다.
                this.variables.add(Variable.builder().name(name).value(variablesMap.get(name).toString()).build());
            }
        }
        else {
            this.variables = Collections.emptyList();
        }
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this.configData);
    }
}
