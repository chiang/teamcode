package io.teamcode.common.ci;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;

import java.net.*;
import java.util.Enumeration;

/**
 * Created by chiang on 2017. 4. 11..
 */
public abstract class RunnerHelper {

    public static final String generateRunnersRegistrationToken() {
        String ipAddress = findOneIpAddress();
        if (StringUtils.hasText(ipAddress)) {
            return DigestUtils.sha1Hex(String.format("%s%s", ipAddress, System.currentTimeMillis())).substring(0, 20);
        }
        else {
            return DigestUtils.sha1Hex(new Long(System.currentTimeMillis()).toString()).substring(0, 20);
        }
    }

    private static final String findOneIpAddress() {
        Enumeration enumeration;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
            while(enumeration.hasMoreElements())  {
                NetworkInterface n = (NetworkInterface) enumeration.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())  {
                    InetAddress i = (InetAddress) ee.nextElement();
                    System.out.println(i.getHostAddress());

                    return i.getHostAddress();
                }
            }
        } catch (SocketException e) {
            //do nothing
        }

        return null;
    }
}
