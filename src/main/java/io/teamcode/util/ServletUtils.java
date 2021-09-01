package io.teamcode.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chiang on 16. 3. 12..
 */
public abstract class ServletUtils {

    public static final String getRealIpAddress(HttpServletRequest servletRequest) {
        //FIXME 아래 코드는 항상 127.0.0.1 을 남긴다.
        //is client behind something?
        String ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = servletRequest.getRemoteAddr();
        }

        return ipAddress;

        //return servletRequest.getRemoteAddr();
    }
}
