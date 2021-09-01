package io.teamcode.web.api;

import io.teamcode.common.ci.ContentRange;
import io.teamcode.common.ci.RunnerRequest;
import io.teamcode.common.ci.VersionInfo;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.PipelineStatus;
import io.teamcode.service.ci.JobService;
import io.teamcode.service.ci.JobTraceService;
import io.teamcode.service.ci.PipelineService;
import io.teamcode.service.ci.RunnerService;
import io.teamcode.web.api.model.ci.JobResponse;
import io.teamcode.web.api.model.ci.UpdateJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 아래와 같이 등록 요청을 할 수 있습니다.

 POST http://localhost:8080/api/v1/runners
 {
 "token": "61b7effb0cfa18b09a7398941182d4e4",
 "info": {
 "name": "gitlab-ci-multi-runner",
 "version": "1.11.0",
 "revision": "39837bca",
 "platform": "linux",
 "architecture": "amd64"
 },
 "last_update": "44edef81ebfb548ab3278abca8d81404"
 }

 *
 *
 */
@RestController
@RequestMapping("/api/v1/runners")
public class RunnerApiController {

    private static final Logger logger = LoggerFactory.getLogger(RunnerApiController.class);

    @Autowired
    RunnerService runnerService;

    @Autowired
    JobService jobService;

    @Autowired
    JobTraceService jobTraceService;

    /**
     * Runner 가 처음 실행될 때 한번 이 API 를 호출합니다. 만약 해당 Runner 로 실행이 되고 있던 파이프라인 들이 있다면 Cancel 시킵니다.
     *
     * @param runnerRequest
     */
    @PostMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RunnerRequest runnerRequest) {

        runnerService.verifyRunnerThenIfNotRegisteredThenRegister(runnerRequest);
    }

    /**
     * 실행할 Job 이 있는지 조회 후 있다면 실행합니다.
     *
     * @param runnerRequest
     */
    @PostMapping(value = "/request", consumes = {"application/json"})
    public ResponseEntity<JobResponse> pickThenRunJob(@RequestBody RunnerRequest runnerRequest) {
        JobResponse jobResponse = runnerService.pickThenRunJobs(runnerRequest);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Job-Status", jobResponse.getJob().getStatus().toString());
        //TODO Case 검증하기
        if (jobResponse.getJob().getCurrentContentRange() != null) {
            responseHeaders.add(HttpHeaders.RANGE, jobResponse.getJob().getCurrentContentRange().toString());
        }

        return new ResponseEntity<>(jobResponse, responseHeaders, HttpStatus.CREATED);
    }

    /**
     *
     * http_codes [[200, 'Job was updated'], [403, 'Forbidden']]
     *
     *
     * Job 을 못 찾으면 404 를 던집니다.
     *
     * @param jobId
     * @param updateJobRequest
     */
    @PutMapping(value = "/jobs/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateJob(@PathVariable Long jobId, @RequestBody UpdateJobRequest updateJobRequest) throws IOException {

        //TODO custom IOException?
        jobService.update(jobId, updateJobRequest);
    }

    @PatchMapping(value = "/jobs/{jobId}/trace", consumes = {"text/plain"})
    public ResponseEntity<String> patchTrace(
            @PathVariable Long jobId,
            @RequestHeader(value=HttpHeaders.CONTENT_RANGE) String contentRangeString,
            @RequestBody String traceBody) {

        //TODO to trace
        logger.debug("content range on patch trace: {}", contentRangeString);

        if (!StringUtils.hasText(contentRangeString)) {
            return new ResponseEntity<>("400 Missing header Content-Range", HttpStatus.BAD_REQUEST);
            //return new ResponseEntity<>(Void, HttpStatus.BAD_REQUEST);
        }

        try {
            Job job = jobTraceService.trace(jobId, ContentRange.build(contentRangeString), traceBody);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Job-Status", job.getStatus().toString());

            logger.debug("content range on patch trace 2: {}", job.getCurrentContentRange());

            if (job.getCurrentContentRange().getLimit() < 0) {
                responseHeaders.add(HttpHeaders.RANGE,
                        ContentRange.builder().offset(0).limit(-job.getCurrentContentRange().getLimit()).build().toString());

                return new ResponseEntity<>("416 Range Not Satisfiable", responseHeaders, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
                //return new ResponseEntity<>(JobResponse.builder().build(), responseHeaders, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
            }

            responseHeaders.add(HttpHeaders.RANGE, job.getCurrentContentRange().toString());

            return new ResponseEntity<>("", responseHeaders, HttpStatus.ACCEPTED);
        } catch (IOException e) {
            //TODO To english?
            //TODO runner 에서 어떻게 처리?
            return new ResponseEntity<>("로그를 저장하던 중 알수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            //return new ResponseEntity<>(JobResponse.builder().build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gitlab 에서는 verify 와 register 가 별도로 호출되지만 우리는 Verify 시 등록되지 않았다면
     * 자동으로 등록해 줍니다.
     *
     */
    public void verifyRunner(String token, VersionInfo runnerVersionInfo) {
        //runnerService.verifyRunnerThenIfNotRegisteredThenRegister(token, runnerVersionInfo);
    }

}
