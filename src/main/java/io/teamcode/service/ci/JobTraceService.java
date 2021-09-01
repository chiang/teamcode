package io.teamcode.service.ci;

import io.teamcode.common.ci.ContentRange;
import io.teamcode.common.ci.JobTrace;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

/**
 *
 */
@Component
public class JobTraceService {

    private static final Logger logger = LoggerFactory.getLogger(JobTraceService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    JobService jobService;

    public File getTrace(final Job job) throws IOException {
        File logFile = getJobTraceFile(job);
        if (!logFile.exists())
            throw new FileNotFoundException(String.format("파일 '%s' 를 찾을 수 없습니다.", logFile.getAbsolutePath()));

        return logFile;
    }

    public Job trace(final Long jobId, final String data, final long offset) throws IOException {
        Job job = jobService.getJob(jobId);

        File logFile = getJobTraceFile(job);
        JobTrace jobTrace = new JobTrace();

        if (offset >= 0) {
            long contentLength = jobTrace.append(logFile, data, offset);
            job.setCurrentContentRange(ContentRange.builder().offset(0).limit(contentLength).build());
            job.setTraceLength(contentLength);
        }
        else {
            long contentLength = jobTrace.append(logFile, data);
            job.setCurrentContentRange(ContentRange.builder().offset(0).limit(contentLength).build());
            job.setTraceLength(contentLength);
        }

        return job;
    }

    public Job trace(final Long jobId, final ContentRange contentRange, final String data) throws IOException {
        Job job = trace(jobId, data, contentRange.getOffset());

        return job;
    }

    private final File getJobTraceFile(final Job job) throws IOException {
        File logsDir = tcConfig.getLogsDir();

        StringBuilder currentPathBuilder = new StringBuilder();
        currentPathBuilder.append(DateUtils.ciBuildsPathFormat(job.getCreatedAt()));
        currentPathBuilder.append(File.separator);
        currentPathBuilder.append(job.getPipeline().getProject().getId());

        File logDir = new File(logsDir, currentPathBuilder.toString());
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                logger.error("로그 디렉터리를 생성할 수 없어 Runner 가 전달한 Trace 를 저장할 수 없습니다.", logDir.getAbsolutePath());

                throw new IOException();
            }
        }

        File logFile = new File(logDir, String.format("%s.log", job.getId()));

        return logFile;
    }

}
