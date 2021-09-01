package io.teamcode.common.io.ansi;

import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ansi Color 로 기록된 로그 파일을 HTML 로 변환합니다. 줄자꿈 등은 적절히 태그 처리합니다.
 */
public abstract class AnsiHtmlConverter {

    public static final String convert(String raw) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            HtmlAnsiOutputStream hos = new HtmlAnsiOutputStream(os);
            hos.write(raw.getBytes("UTF-8"));
            hos.close();
            return new String(os.toByteArray(), "UTF-8");
        }
    }

}
