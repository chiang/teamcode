package io.teamcode.common.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * Created by chiang on 2017. 4. 3..
 */
public abstract class ClasspathResourceReader {

    public static final InputStream getInputStream(final String path) {
        //FIXME /가 붙어서 오는 경우도 처리 필요?
        return ClasspathResourceReader.class.getResourceAsStream(String.format("/%s", path));
    }

    /**
     * 아래와 같이 읽습니다. 파일을 읽어서 문자열로 반환합니다.
     *
     * <code>ClasspathResourceReader.readAsString("META-INF/hook-scripts/pre-revprop-change");</code>
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static final String readAsString(final String path) throws IOException {
        InputStream inputStream = null;
        try {
            //FIXME /가 붙어서 오는 경우도 처리 필요?
            inputStream = ClasspathResourceReader.class.getResourceAsStream(String.format("/%s", path));
            if (inputStream != null) {
                StringWriter writer = new StringWriter();
                IOUtils.copy(inputStream, writer, Charset.defaultCharset());
                return writer.toString();
            } else {
                throw new IOException(String.format("클래스패스에 있는 리소스 '%s' 를 읽을 수 없습니다. 파일이 없을 수 있습니다.", path));
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
