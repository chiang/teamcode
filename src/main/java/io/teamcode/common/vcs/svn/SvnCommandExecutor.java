package io.teamcode.common.vcs.svn;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by chiang on 2017. 3. 25..
 */
public abstract class SvnCommandExecutor {

    /**
     *
     *
     * @param repositoryUri
     * @param revisionNumber
     * @return
     * @throws IOException
     */
    public static final String diff(final String repositoryUri, final long revisionNumber) throws IOException {
        CommandLine cmdLine = createCmdLine()
                .addArgument("diff")
                .addArgument(String.format("-c%s", revisionNumber))
                .addArgument(repositoryUri);

        return executeCommand(cmdLine);
    }

    private static final String executeCommand(CommandLine cmdLine)
            throws ExecuteException,IOException {
        DefaultExecutor exec = new DefaultExecutor();
        //exec.setWorkingDirectory(new File("/usr/local/subversion-1.8/bin"));

        String str ="";
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            exec.setStreamHandler(new PumpStreamHandler(outputStream));
            int exitCode = exec.execute(cmdLine);
            str =  outputStream.toString();
        }

        return str;
    }

    private static final CommandLine createCmdLine() {
        return CommandLine.parse("svn");
    }
}
