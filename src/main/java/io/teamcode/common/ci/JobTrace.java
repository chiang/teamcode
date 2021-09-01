package io.teamcode.common.ci;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 빌드 시 Job 의 로그 정보를 처리하는 클래스.
 */
public class JobTrace {

    /*
    def append(data, offset)
        write do |stream|
          current_length = stream.size
          return -current_length unless current_length == offset

          data = job.hide_secrets(data)
          stream.append(data, offset)
          stream.size
        end
      end

     */

    /**
     * 로그 파일에 내용을 추가한 후 최종 파일의 크기를 돌려줍니다. 만약 음수이면 정상적으로 처리가 되지 않았으며 해당 파일의 최근
     * 크기를 돌려줍니다.
     *
     * @param data
     * @param offset
     * @return
     * @throws IOException
     */
    public long append(final File logFile, String data, long offset) throws IOException {
        if (offset == 0) {//이렇게 올 일은 없겠지만 다시 시작하는 빌드가 그럴 수 있다.
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, false))) {
                bw.write(data);
                //bw.write("\n");

                return data.length();
            }
        }
        else {
            long contentLength = logFile.length();
            if (contentLength != offset)
                return -contentLength;

            return append(logFile, data);
        }

        //return contentLength + data.length();
    }

    public long append(final File logFile, String data) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            long contentLength = logFile.length();

            bw.write(data);

            return contentLength + data.length();
        }
    }
}
