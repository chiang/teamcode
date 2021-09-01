package io.teamcode.common.io.ansi;

import io.teamcode.common.io.ansi.HtmlAnsiOutputStream;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chiang on 2017. 5. 15..
 */
public class AnsiHtmlConverterTest {

    public static final String ANSI_BOLD_RED     = "\033[31;1m";
    public static final String ANSI_RESET        = "\033[0;m";

    @Test
    public void convert() throws IOException {

        String raw = String.format("%sUsing docker image...%s", ANSI_BOLD_RED, ANSI_RESET);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HtmlAnsiOutputStream hos = new HtmlAnsiOutputStream(os);
        hos.write(raw.getBytes("UTF-8"));
        hos.close();
        System.out.println(">> " + new String(os.toByteArray(), "UTF-8"));
    }
}
