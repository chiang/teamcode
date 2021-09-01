package io.teamcode.web.ui.view.ci;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Data
//@JsonRootName(value = "count")
public class PipelinesCountView {

    private int all;

    private int finished;

    private int pending;

    private int running;

}
