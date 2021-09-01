package io.teamcode.common.ci.config;

import io.teamcode.common.ci.config.entry.Global;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.common.ci.config.entry.StagesEntry;
import io.teamcode.web.api.model.ci.Variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class CiConfig {

    private static Map<String, String> keywords = new LinkedHashMap<>();

    private static final String KEY_VARIABLES = "variables";

    static {
        keywords.put("image", "Use docker image, covered in Use Docker");
        keywords.put("services", "Use docker services, covered in Use Docker");
        keywords.put("stages", "Define build stages");
        keywords.put("before_script", "Use docker image, covered in Use Docker");
        keywords.put("after_script", "Use docker image, covered in Use Docker");
        keywords.put(KEY_VARIABLES, "Use docker image, covered in Use Docker");
        keywords.put("cache", "Use docker image, covered in Use Docker");
    }

    private ValidationContext validationContext;

    private Map<String, Object> map;

    private Global global;

    public CiConfig(Map<String, Object> map) {
        this.map = map;
        this.global = new Global(keywords, new ConcurrentHashMap<>(map));
        this.validationContext  = new ValidationContext(map);
    }

    public void validate() {
        global.validate(validationContext);
    }

    public boolean hasErrors() {

        return this.getValidationContext().getValidationError().size() > 0;
    }

    public ValidationContext getValidationContext() {
        return this.validationContext;
    }

    public String getImage() {
        if (map.containsKey("image")) {
            return (String)map.get("image");
        }
        else {
            return null;
        }
    }


    public List<Variable> getVariables() {
        if (map.containsKey(KEY_VARIABLES)) {
            Map<String, String> variablesMap = (Map<String, String>)map.get(KEY_VARIABLES);

            List<Variable> variables = new ArrayList<>();
            for (String key: variablesMap.keySet()) {
                variables.add(Variable.builder().name(key).value(variablesMap.get(key)).build());
            }

            return variables;
        }
        else {
            return Collections.emptyList();
        }
    }

    public StagesEntry getStagesEntry() {

        return global.getStagesEntry();
    }

    public List<JobEntry> getJobEntries() {

        return global.getJobEntries();
    }

}
