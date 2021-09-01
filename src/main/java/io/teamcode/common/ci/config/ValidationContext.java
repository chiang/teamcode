package io.teamcode.common.ci.config;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class ValidationContext {

    private Map<String, Object> configuration;

    private List<String> validationError = new LinkedList<>();

    public ValidationContext(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getConfiguration() {
        return this.configuration;
    }

    public void addError(String error) {
        this.validationError.add(error);
    }

    public List<String> getValidationError() {

        return this.validationError;
    }
}
