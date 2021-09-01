package io.teamcode.common;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chiang on 2017. 4. 2..
 */
public abstract class SystemComponentHelper {

    private static final Logger logger = LoggerFactory.getLogger(SystemComponentHelper.class);

    public static final String getHttpdVersion() {
        CommandLine cmdLine = CommandLine.parse("httpd").addArgument("-v");
        DefaultExecutor exec = new DefaultExecutor();
        //exec.setWorkingDirectory(new File("/usr/local/subversion-1.8/bin"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exec.setStreamHandler(new PumpStreamHandler(outputStream));
            int exitCode = exec.execute(cmdLine);
            if (exitCode == 0) {
                String rawVersionString = outputStream.toString();
                String[] lines = rawVersionString.split(System.getProperty("line.separator"));
                String versionLine = lines[0];

                if (versionLine.startsWith("Server version:"))
                    return versionLine.substring("Server version: ".length(), versionLine.length());
                else
                    return versionLine;
            }
            else {
                return "N/A";
            }
        } catch(IOException e) {
            logger.error("오류가 발생하여 시스템에 설치된 HTTPD 버전을 확인할 수 없습니다.", e);
        } catch(Throwable t) {
            logger.error("오류가 발생하여 시스템에 설치된 HTTPD 버전을 확인할 수 없습니다.", t);
        }

        return "N/A";
    }

    public static final String getPythonVersion() {
        CommandLine cmdLine = CommandLine.parse("python").addArgument("--version");
        DefaultExecutor exec = new DefaultExecutor();
        //exec.setWorkingDirectory(new File("/usr/local/subversion-1.8/bin"));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exec.setStreamHandler(new PumpStreamHandler(outputStream));
            int exitCode = exec.execute(cmdLine);
            if (exitCode == 0) {
                String rawVersionString = outputStream.toString();
                if (rawVersionString.startsWith("Python"))
                    return rawVersionString.substring("Python ".length(), rawVersionString.length() - 1);
                else
                    return rawVersionString;
            }
            else {
                return "N/A";
            }
        } catch(IOException e) {
            logger.error("오류가 발생하여 시스템에 설치된 파이썬 버전을 확인할 수 없습니다.", e);
        } catch(Throwable t) {
            logger.error("오류가 발생하여 시스템에 설치된 파이썬 버전을 확인할 수 없습니다.", t);
        }

        return "N/A";
    }

    //FIXME 오류 남.
    public static final String getDockerVersion() {
        CommandLine cmdLine = CommandLine.parse("docker").addArgument("-v");
        DefaultExecutor exec = new DefaultExecutor();

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exec.setStreamHandler(new PumpStreamHandler(outputStream));
            int exitCode = exec.execute(cmdLine);
            if (exitCode == 0) {
                String rawVersionString = outputStream.toString();
                if (rawVersionString.startsWith("Docker version "))
                    return rawVersionString.substring("Docker version  ".length(), rawVersionString.length() - 1);
                else
                    return rawVersionString;
            }
            else {
                return "N/A";
            }
        } catch(IOException e) {
            logger.error("오류가 발생하여 시스템에 설치된 파이썬 버전을 확인할 수 없습니다.", e);
        } catch(Throwable t) {
            logger.error("오류가 발생하여 시스템에 설치된 파이썬 버전을 확인할 수 없습니다.", t);
        }

        return "N/A";
    }
}
