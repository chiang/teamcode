package io.teamcode.common.io;

import io.teamcode.common.converter.KnownFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 6. 28..
 */
public class KnownFilesResolverTest {

    @Test
    public void resolve() throws IOException, KnownFilesResolveException {
        KnownFilesResolver knownFilesResolver = new KnownFilesResolver();
        knownFilesResolver.read(KnownFilesResolver.class.getResourceAsStream("/io/teamcode/known-file-names.xml"));

        List<String> testFileNames = Arrays.asList("aspnetapp.csproj", ".classpath");

        for (String fileName: testFileNames) {
            Assert.assertTrue(knownFilesResolver.match(fileName));
        }
    }
}
