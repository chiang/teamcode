package io.teamcode.common.ci.config.entry;

import io.teamcode.common.ci.config.Validatable;
import io.teamcode.common.ci.config.ValidationContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 12..
 */
public class Script implements Validatable {

    public static final String KEY = "script";

    @Getter
    private List<String> commands;

    public Script(Object data) {
        if (!(data instanceof List)) {
            throw new ValidationError("script 설정 포멧이 잘못되었습니다.");
        }

        this.commands = (List)data;
    }

    @Override
    public void validate(ValidationContext context) {

        //do nothing...
    }
}
