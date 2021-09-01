package io.teamcode.domain.entity;

/**
 * Created by chiang on 2017. 2. 27..
 */
public enum ProjectRole {

    GUEST("Guest"),
    REPORTER("Reporter"),
    DEVELOPER("Developer"),
    MASTER("Master"),
    OWNER("Owner");

    private String name;

    ProjectRole(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasWriteRole() {

        switch(this) {
            case DEVELOPER:
            case MASTER:
            case OWNER:
                return true;

            default:
                return false;
        }
    }

}
