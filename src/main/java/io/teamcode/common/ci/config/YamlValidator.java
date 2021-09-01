package io.teamcode.common.ci.config;

import java.util.Map;

/**
 * TODO move to another common package?
 *
 * Created by chiang on 2017. 4. 12..
 */
public abstract class YamlValidator {

    public static final void presence(ValidationContext validationContext, Map<String, Object> data, String key) {
        if (data == null && data.size() == 0) {
            validationContext.addError(String.format("%s 가 정의되어 있어야 합니다.", key));
        }
        else {
            if (data.containsKey(key)) {
                validationContext.addError(String.format("%s 가 정의되어 있어야 합니다.", key));
            }
        }
    }
}
