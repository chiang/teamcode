package io.teamcode.web.ui.view;

import lombok.Data;

/**
 * UI 에서 년, 월, 일, 분 을 입력하는 폼을 표현합니다.
 */
@Data
public class DateRangeForm {

    private int startYear;

    private int startMonth;

    private int startDay;

    private int startHour;

    private int startMinute;

    private int endYear;

    private int endMonth;

    private int endDay;

    private int endHour;

    private int endMinute;

}
