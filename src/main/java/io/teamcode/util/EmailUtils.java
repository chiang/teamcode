package io.teamcode.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 2. 4..
 */
public abstract class EmailUtils {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    private static Matcher matcher;

    /**
     * Validate hex with regular expression
     *
     * @param email
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public static final boolean validate(final String email) {

        matcher = pattern.matcher(email);
        return matcher.matches();

    }
}
