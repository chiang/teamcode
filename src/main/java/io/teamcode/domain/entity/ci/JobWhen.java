package io.teamcode.domain.entity.ci;

/**
 * Created by chiang on 2017. 4. 12..
 */
public enum JobWhen {

    ON_SUCCESS("execute job only when all jobs from prior stages succeed. This is the default."),
    ON_FAILURE("execute job only when at least one job from prior stages fails."),
    ALWAYS("execute job regardless of the status of jobs from prior stages."),
    MANUAL("execute job manually (added in GitLab 8.10). Read about manual actions below.");

    private String description;

    JobWhen(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
