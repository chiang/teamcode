package io.teamcode.web.ui.view.ci;

import io.teamcode.domain.entity.ci.Runner;
import lombok.Data;

/**
 * Created by chiang on 2017. 4. 23..
 */
@Data
public class RunnersView {

    private String currentToken;

    private Iterable<Runner> runners;
}
