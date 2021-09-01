package io.teamcode.common.converter;

import org.apache.commons.io.FilenameUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chiang on 2016. 1. 6..
 */
public class MarkdownConverter {

    private static final List<String> README_EXTENSIONS = Arrays.asList("md", "mkd", "mkdn", "mdown", "markdown");

    public static final boolean isMarkdownFile(final String fileName) {
        if (StringUtils.hasText(fileName)) {
            String name = fileName.toLowerCase();
            String extension = FilenameUtils.getExtension(name);
            if (StringUtils.hasText(extension)) {
                return README_EXTENSIONS.contains(extension);
            }
        }

        return false;
    }

    public static final boolean isReadme(String fileName) {
        if (StringUtils.hasText(fileName)) {
            String name = fileName.toLowerCase();
            if (name.startsWith("readme")) {
                String extension = FilenameUtils.getExtension(name);
                if (StringUtils.hasText(extension)) {
                    return README_EXTENSIONS.contains(extension);
                }

                return true;
            }
        }

        return false;
    }

    public static final String convert(String content) throws ReadmeConversionException {
        if (StringUtils.hasText(content)) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(content);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
        }
        else {
            return "";
        }
    }

    public static final String convert(String content, String extension) throws ReadmeConversionException {
        if (StringUtils.hasText(content)) {

            if (StringUtils.hasText(extension)) {
                if (extension.toLowerCase().equals("text") || extension.toLowerCase().equals("text")) {
                    return convertPlainReadme(content);
                }
                else if (extension.toLowerCase().equals("md")) {
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(content);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    return renderer.render(document);  // "<p>This is <em>Sparta</em></p>\n"
                }
            }

            return convertPlainReadme(content);
        }
        else {
            return "";
        }
    }

    //no extension, .text, .txt
    private static final String convertPlainReadme(String content) {
        return new StringBuilder("<pre>").append(content).append("</pre>").toString();
    }
}
