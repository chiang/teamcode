package io.teamcode.domain;

/**
 * 사용자 정의 테마 정보입니다.
 *
 */
public enum Theme {

    GRAPHITE("Graphite", "ui_graphite"),
    CHARCOAL("Charcoal", "ui_charcoal"),
    GREEN("Green", "ui_green"),
    BLACK("Black", "ui_black"),
    VIOLET("Violet", "ui_violet"),
    BLUE("Blue", "ui_blue");

    private String title;

    private String cssClass;

    Theme(final String title, final String cssClass) {
        this.title = title;
        this.cssClass = cssClass;
    }

    public String getTitle() {
        return this.title;
    }

    public String getCssClass() {
        return this.cssClass;
    }

}
