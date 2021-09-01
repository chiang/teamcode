package io.teamcode.common.vcs.svn;

import io.teamcode.common.vcs.DiffParser;
import io.teamcode.common.vcs.svn.diff.SvnDiffParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 25..
 */
public class SvnDiffParserTest {

    @Test
    public void parse() throws IOException {
        /*InputStream sampleDiff =  SvnDiffParserTest.class.getResourceAsStream("/data/sample.diff");
        String content = IOUtils.toString(sampleDiff, "UTF-8");

        Diff diff = SvnDiffParser.parse2(content);
        Assert.assertEquals(3, diff.getDetails().size());*/

        InputStream sampleDiff =  SvnDiffParserTest.class.getResourceAsStream("/data/sample.diff");
        DiffParser diffParser = new SvnDiffParser();
        List<Diff> diffs = diffParser.parse(sampleDiff);
        Assert.assertEquals(3, diffs.size());
    }

    @Test
    public void testSingleDiff() throws Exception {
        // given
        DiffParser parser = new SvnDiffParser();
        String in = ""
                + "--- from	2015-12-21 17:53:29.082877088 -0500\n"
                + "+++ to	2015-12-21 08:41:52.663714666 -0500\n"
                + "@@ -10,1 +10,1 @@\n"
                + "-from\n"
                + "+to\n";

        // when
        List<Diff> diffs = parser.parse(in.getBytes());

        // then
        Assert.assertNotNull(diffs);
        Assert.assertEquals(1, diffs.size());

        Diff diff1 = diffs.get(0);
        Assert.assertEquals(1, diff1.getHunks().size());
    }

    @Test
    public void testNormalDiff() throws Exception {
        InputStream inputStream =  SvnDiffParserTest.class.getResourceAsStream("/data/svn-normal.diff");

        DiffParser parser = new SvnDiffParser();
        // when
        List<Diff> diffs = parser.parse(inputStream);

        // then
        Assert.assertNotNull(diffs);
        Assert.assertEquals(2, diffs.size());

        Diff diff1 = diffs.get(0);
        Assert.assertEquals(1, diff1.getHunks().size());
        Assert.assertEquals("RELEASE.txt", diff1.getFileName());
        Assert.assertEquals(1, diff1.getAdditions());
    }
}
