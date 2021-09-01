package io.teamcode.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 3. 27..
 */
public class UrlUtilsTest {

    @Test
    public void encodeURIComponent() {
        String fileName = "한글_a.hwp";

        String encodedFileName = UrlUtils.encodeURIComponent(fileName);
        Assert.assertEquals(fileName, UrlUtils.decodeURIComponent(encodedFileName));
    }
}
