package io.teamcode.common;

import io.teamcode.domain.entity.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 3. 23..
 */
public class RegexTest {

    @Test
    public void projectPath() {
        String projectPathPattern = "^[\\p{Alnum}\\-]{2,32}$";
        Pattern p = Pattern.compile(projectPathPattern);
        Assert.assertTrue(p.matcher("project").find());
        Assert.assertTrue(p.matcher("project-2").find());
        Assert.assertTrue(p.matcher("project-2-a").find());

        Assert.assertFalse(p.matcher(" project-2-a").find());
        Assert.assertFalse(p.matcher(" 한project-2-a").find());
    }

    @Test
    public void defaultIssueId() {
        String ticketId = "이슈 #425 처리";
        String ticketPattern = "#(\\d+)";

        Pattern p = Pattern.compile(ticketPattern);
        Matcher m = p.matcher(ticketId);
        Assert.assertTrue(m.find());
        Assert.assertEquals("425", m.group(1));

        m = p.matcher("이슈 아이디 #5313-4123");
        Assert.assertTrue(m.find());
        Assert.assertEquals("5313", m.group(1));

        m = p.matcher("이슈 아이디 #4123");
        Assert.assertTrue(m.find());
        Assert.assertEquals("4123", m.group(1));

        m = p.matcher("#4123 이슈 처리");
        Assert.assertTrue(m.find());
        Assert.assertEquals("4123", m.group(1));

        m = p.matcher("  #4123 이슈 처리");
        Assert.assertTrue(m.find());
        Assert.assertEquals("4123", m.group(1));

        m = p.matcher("  4123 이슈 처리");
        Assert.assertFalse(m.find());
    }

    @Test
    public void jira() {
        String ticketId = "abaf [JIRA-312] afafa";
        String ticketPattern = "\\[(JIRA)-(\\d+)\\]";

        Pattern p = Pattern.compile(ticketPattern);
        Matcher m = p.matcher(ticketId);
        Assert.assertTrue(m.find());
        Assert.assertEquals("[JIRA-312]", m.group());
        Assert.assertEquals("[JIRA-312]", m.group(0));
        Assert.assertEquals("JIRA", m.group(1));
        Assert.assertEquals("312", m.group(2));
    }

    @Test
    public void skipCi() {
        String skipCiRegex = "\\[(ci[ _-]skip|skip[ _-]ci)\\]";

        String message = "[skip_ci] 하하";

        Pattern p = Pattern.compile(skipCiRegex);
        Matcher m = p.matcher(message);
        Assert.assertTrue(m.find());

        message = "후후 [skip ci] 하";
        m = p.matcher(message);
        Assert.assertTrue(m.find());

        message = "후후 [skip_ci] 하";
        m = p.matcher(message);
        Assert.assertTrue(m.find());

        message = "후후 [skip-ci] 하";
        m = p.matcher(message);
        Assert.assertTrue(m.find());

        message = "후후 [skip-ci ] 하";
        m = p.matcher(message);
        Assert.assertFalse(m.find());

        try {
            Class clazz = Class.forName(new User().getClass().getCanonicalName());
            System.out.println("--> " + clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
