package io.teamcode.domain.entity;

/**
 *
 */
public enum IssueState {

    OPENED("Open"),
    CLOSED("Closed");

    private String displayName;

    IssueState(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
