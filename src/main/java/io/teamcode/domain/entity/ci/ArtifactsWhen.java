package io.teamcode.domain.entity.ci;

/**
 * Created by chiang on 2017. 6. 28..
 */
public enum ArtifactsWhen {

    ON_SUCCESS("Upload artifacts only when the job succeeds. This is the default."),
    ON_FAILURE("Upload artifacts only when the job fails."),
    ALWAYS("Upload artifacts regardless of the job status.");

    private String description;

    ArtifactsWhen(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
