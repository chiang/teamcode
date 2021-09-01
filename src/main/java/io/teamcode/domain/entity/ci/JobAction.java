package io.teamcode.domain.entity.ci;

/**
 * Created by chiang on 2017. 5. 14..
 */
public enum JobAction {

    PLAY("icon_action_play"),
    CANCEL("icon_action_cancel"),
    STOP("icon_action_stop"),
    RETRY("icon_action_retry");

    private String icon;

    JobAction(final String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return this.icon;
    }
}
