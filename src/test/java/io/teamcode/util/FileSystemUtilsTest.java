package io.teamcode.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by chiang on 2017. 3. 18..
 */
public class FileSystemUtilsTest {

    @Test
    public void detectMimeType() throws IOException {
        File image = new File("./src/test/resources/sample-changedFiles/404.png");
        String mimeType = FileSystemUtils.detectMimeType(image);
        System.out.println("---> " + mimeType);

        image = new File("./src/test/resources/sample-changedFiles/fake-image.png");
        mimeType = FileSystemUtils.detectMimeType(image);
        Assert.assertEquals("image/png", mimeType);

        image = new File("./src/test/resources/sample-changedFiles/README.txt");
        mimeType = FileSystemUtils.detectMimeType(image);
        Assert.assertEquals("text/plain", mimeType);

        mimeType = FileSystemUtils.detectMimeType("README.txt");
        Assert.assertEquals("text/plain", mimeType);
    }
}
