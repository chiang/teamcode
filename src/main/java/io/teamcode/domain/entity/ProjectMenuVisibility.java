package io.teamcode.domain.entity;

/**
 * Created by chiang on 2017. 4. 9..
 */
public enum ProjectMenuVisibility {

    HIDDEN("숨김"), ENABLED("사용"), DISABLED("사용 안함");

    private String title;

    ProjectMenuVisibility(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
