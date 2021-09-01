package io.teamcode.common;

import org.junit.Test;

/**
 * Created by chiang on 2017. 4. 2..
 */
public class SystemComponentHelperTest {

    @Test
    public void getPythonVersion() {
        String version = SystemComponentHelper.getPythonVersion();
        System.out.println("--> version: [" + version + "]");
    }

    @Test
    public void getHttpdVersion() {
        String version = SystemComponentHelper.getHttpdVersion();
        System.out.println("--> version: [" + version + "]");
    }

    /*@Test
    public void getDockerVersion() {
        String version = SystemComponentHelper.getDockerVersion();
        System.out.println("--> version: [" + version + "]");
    }*/
}
