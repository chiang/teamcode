package io.teamcode.domain;

import lombok.Data;

import java.util.List;

/**
 * Created by chiang on 2017. 2. 5..
 */
@Data
public class NotificationEmails {

    private String currentNotificationEmail;

    private List<String> userEmails;

}
