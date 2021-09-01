package io.teamcode.domain;

/**
 * Created by chiang on 2017. 2. 5..
 */
public enum ApplicationLayout {

    FIXED("Fixed"), FLUID("Fluid");

    private String name;

    ApplicationLayout(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
