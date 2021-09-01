package io.teamcode.common.ci;

import lombok.Data;

/**
 * Created by chiang on 2017. 5. 11..
 */
@Data
public class FeaturesInfo {

    private boolean variables;

    private boolean image;

    private boolean services;

    //Artifacts
    private boolean features;

    private boolean cache;

}
