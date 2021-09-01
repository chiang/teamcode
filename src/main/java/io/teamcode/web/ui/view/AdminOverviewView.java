package io.teamcode.web.ui.view;

import lombok.Data;

/**
 * Created by chiang on 2017. 4. 2..
 */
@Data

public class AdminOverviewView {

    private Long projectsCount;

    private Long usersCount;

    private Long attachmentsCount;

    private Long activeUsersCount;

    private String javaVersion = System.getProperty("java.version");

    private String javaVendor = System.getProperty("java.vendor");

    private String subversionVersion;

    private String httpdVersion;

    private String pythonVersion;

    private String databaseProductName;

    private String databaseVersion;

}
