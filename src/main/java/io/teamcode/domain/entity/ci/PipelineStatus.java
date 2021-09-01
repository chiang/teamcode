package io.teamcode.domain.entity.ci;

/**
 * Created by chiang on 2017. 4. 9..
 */
public enum PipelineStatus {

    CREATED("created", "created", "icon_status_created", "favicon_status_created", null),
    //RUNNING("running", "running", "icon_status_running", "favicon_status_running", JobAction.CANCEL),//TODO Cancel 테스트가 끝나면...
    RUNNING("running", "running", "icon_status_running", "favicon_status_running", null),
    SKIPPED("skipped", "skipped", "icon_status_skipped", "favicon_status_skipped", null),
    FAILED("failed", "failed", "icon_status_failed", "favicon_status_failed", JobAction.RETRY),
    CANCELED("canceled", "canceled", "icon_status_canceled", "favicon_status_canceled", JobAction.RETRY),
    MANUAL("manual", "manual action", "icon_status_manual", "favicon_status_manual", JobAction.PLAY),
    PENDING("pending", "pending", "icon_status_pending", "favicon_status_pending", null),
    SUCCESS("passed", "passed", "icon_status_success", "favicon_status_success", JobAction.RETRY),
    SUCCESS_WITH_WARNINGS("passed", "passed with warnings", "icon_status_warning", "", null);

    private String text;

    private String label;

    private String icon;

    private String favicon;

    private JobAction jobAction;

    PipelineStatus(String text, String label, String icon, String favicon, final JobAction jobAction) {
        this.text = text;
        this.label = label;
        this.icon = icon;
        this.favicon = favicon;
        this.jobAction = jobAction;
    }

    public String getGroup() {
        if (this == SUCCESS_WITH_WARNINGS)
            return "success_with_warnings";
        else
            return this.name().toLowerCase();
    }

    public String getText() {
        return this.text;
    }

    public String getLabel() {
        return this.label;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getFavicon() {
        return this.favicon;
    }

    public boolean isCompletedStatus() {
        switch (this) {
            case SUCCESS:
            case FAILED:
            case CANCELED:
            case SKIPPED:
                return true;

            default:
                return false;
        }
    }

    public boolean isHasAction() {
        return this.jobAction != null;
    }

    public JobAction getJobAction() {
        return this.jobAction;
    }

}
