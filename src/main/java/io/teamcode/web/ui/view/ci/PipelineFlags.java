package io.teamcode.web.ui.view.ci;

import lombok.Data;

/**
 * Created by chiang on 2017. 4. 16..
 */
@Data
public class PipelineFlags {

    private boolean cancelable = false;

    private boolean latest = false;

    private boolean retryable = false;

    private boolean stuck = false;

    private boolean triggered = false;

    private boolean yamlErrors = false;

}
