package io.teamcode.util;

import org.springframework.util.StringUtils;

/**
 * Created by chiang on 2017. 3. 25..
 */
public abstract class MimeTypeUtils {

    public static final boolean isImage(final String mimeType) {
        if (StringUtils.hasText(mimeType)) {
            return mimeType.startsWith("image/");
        }

        return false;
    }

}
