package io.teamcode.common;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chiang on 2017. 3. 25..
 */
public class MarkdownTest {

    @Test
    public void parse() {
        Parser parser = Parser.builder().build();
        Node document = parser.parse("This is *Sparta*");
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        Assert.assertEquals("<p>This is <em>Sparta</em></p>\n", html);
    }
}
