package io.teamcode.domain.entity;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Created by chiang on 2017. 2. 6..
 */
public enum Visibility {

    PRIVATE,
    INTERNAL,
    PUBLIC;

    public String getTooltip() {
        return new StringBuilder(WordUtils.capitalize(this.name().toLowerCase())).append(" - ").append(getProjectVisibilityLevelDescription()).toString();
    }

    public String getProjectVisibilityLevelDescription() {

        switch(this) {
            case PUBLIC:
                //return "The project can be cloned without any authentication.";
                return "이 프로젝트는 로그인한 모든 사용자가 접근 가능합니다.";

            case PRIVATE:
                return "Project access must be granted explicitly to each user.";

            case INTERNAL:
                return "이 프로젝트는 프로젝트 팀에 속한 사용자만 접근 가능합니다.";
            default:
                return null;
        }
    }
}
