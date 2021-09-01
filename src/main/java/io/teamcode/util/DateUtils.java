package io.teamcode.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chiang on 2017. 4. 2..
 */
public abstract class DateUtils {

    private static final DateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

    private static final DateFormat MONTH_FORMAT = new SimpleDateFormat("MM");

    public static final Date parseMilestoneDateString(final String dateString) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.parse(dateString);
    }

    public static final String defaultBackupSuffix() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        return dateFormat.format(new Date());
    }

    public static final String archivedProjectNameSuffix() {
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");

        return dateFormat.format(new Date());
    }

    public static final String ciBuildsPathFormat(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");

        return dateFormat.format(new Date());
    }

    public static final String[] dateBasedDirectoryStructure() {
        Date now = new Date();

        //TODO date formatter thread safe?
        return new String[]{YEAR_FORMAT.format(now), MONTH_FORMAT.format(now)};
    }
}
