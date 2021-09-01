package io.teamcode.service.ci;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.teamcode.common.ci.config.entry.JobEntry;
import io.teamcode.common.ci.config.entry.StageEntry;
import io.teamcode.common.ci.config.entry.StagesEntry;
import io.teamcode.common.io.ansi.AnsiHtmlConverter;
import io.teamcode.domain.entity.ci.*;
import io.teamcode.repository.JobRepository;
import io.teamcode.service.InsufficientPrivilegeException;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.vcs.svn.CommitService;
import io.teamcode.web.api.model.ci.Artifact;
import io.teamcode.web.api.model.ci.Dependency;
import io.teamcode.web.api.model.ci.DependencyArtifactsFile;
import io.teamcode.web.api.model.ci.UpdateJobRequest;
import io.teamcode.web.ui.view.ci.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

/**
 * Created by chiang on 2017. 4. 15..
 */
@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobProcessService jobProcessService;

    @Autowired
    JobTraceService jobTraceService;

    @Autowired
    CommitService commitService;

    /**
     * 단순히 상태 값만을 업데이트합니다.
     *
     * @param job
     * @param jobStatus
     */
    @Transactional
    public void updateStatus(final Job job, final PipelineStatus jobStatus) {
        Job j = getJob(job.getId());
        j.setStatus(jobStatus);
        jobRepository.save(j);

        logger.debug("Job #{} ({}) 상태를 '{}' 로 업데이트했습니다...", j.getId(), j.getName(), jobStatus);
    }

    @Transactional
    public void play(final Long jobId) {
        Job job = getJob(jobId);
        logger.info("Job #{} ({}) 을 Play 합니다...", job.getId(), job.getName());

        jobProcessService.play(job);
    }

    @Transactional
    public void retry(final Long jobId) {
        Job job = getJob(jobId);
        logger.info("Job #{} ({} with status {}) 을 Retry 합니다...", job.getId(), job.getName(), job.getStatus());

        jobProcessService.retry(job);
    }

    @Transactional
    public void cancel(final Long jobId) {
        Job job = getJob(jobId);
        logger.info("Job #{} ({} with status {}) 을 Cancel 합니다...", job.getId(), job.getName(), job.getStatus());

        jobProcessService.cancel(job);
    }

    /**
     *
     *
     * @param runner
     */
    @Transactional
    public long cancelActiveJobs(final Runner runner) {
        List<Job> jobs = jobRepository.findByRunner(runner);
        if (!jobs.isEmpty()) {
            logger.info("Runner #{} 에 할당된 Active Job 이 총 '{}' 개 있습니다. 이 Job 들을 모두 Cancel 처리합니다...",
                    runner.getId(),
                    jobs.stream().filter(j -> j.isActive()).count());
            jobs.stream().filter(j -> j.isActive()).forEach(j -> jobProcessService.cancel(j));

            return jobs.stream().filter(j -> j.isActive()).count();
        }

        return 0;
    }

    @Transactional(readOnly = true)
    public Job getJob(final Long jobId) {
        Job job = jobRepository.findOne(jobId);//TODO authenticate_job!?
        if (job == null)
            throw new ResourceNotFoundException(String.format("Job #%S 를 찾을 수 없습니다.", jobId));

        return job;
    }

    @Transactional(readOnly = true)
    public JobView getJobView(final String groupPath, final String projectPath, final Long jobId) {
        Job job = getJob(jobId);
        Pipeline pipeline = job.getPipeline();
        pipeline.setJobs(getLatestJobs(pipeline));

        JobView jobView = new JobView();
        jobView.setPipelineId(pipeline.getId());
        jobView.setJob(job);
        jobView.setPipelineLink(String.format("/projects/%s/pipelines/%s", pipeline.getProject().getPath(), pipeline.getId()));
        jobView.setJobTraceLink(String.format("/projects/%s/jobs/%s/trace.json",
                job.getPipeline().getProject().getPath(), job.getId()));
        jobView.setCommit(commitService.getCommit(projectPath, pipeline.getCommitRevision()));
        jobView.setCommitLink(String.format("/projects/%s/commits/%s", pipeline.getProject().getPath(), jobView.getCommit().getRevision()));

        PipelineStageView pipelineStageView;
        String jobActionPath = null;
        for(Stage stage: pipeline.getStages(true)) {
            pipelineStageView = new PipelineStageView();
            pipelineStageView.setName(stage.getName());
            pipelineStageView.setStatus(PipelineStatusView.builder().pipelineStatus(stage.getStatus()).build());

            for (Job j: stage.getJobs()) {
                if (j.getStatus().isHasAction())
                    jobActionPath = j.getStatus().getJobAction().name().toLowerCase();
                pipelineStageView.addPipelineJobView(
                        PipelineJobView.builder()
                                .id(j.getId())
                                .name(j.getName())
                                .duration(j.resolveDuration())
                                .status(JobStatusView.builder().jobStatus(j.getStatus()).build())
                                .path(String.format("/%s/%s/jobs/%s", groupPath, projectPath, j.getId()))
                                .actionPath(String.format("/%s/%s/jobs/%s/%s", groupPath, projectPath, j.getId(), jobActionPath))
                                .build());
            }

            jobView.addStage(pipelineStageView);
        }

        return jobView;
    }

    public JobTraceView getJobTraceView(final Long jobId) {
        Job job = getJob(jobId);

        JobTraceView jobTraceView = new JobTraceView();
        jobTraceView.setId(job.getId());
        jobTraceView.setStatus(job.getStatus());
        jobTraceView.setComplete(job.isCompleted());

        try {
            File traceFile = jobTraceService.getTrace(job);
            jobTraceView.setOffset(0);
            jobTraceView.setSize(traceFile.length());
            jobTraceView.setHtml(AnsiHtmlConverter.convert(FileUtils.readFileToString(traceFile, "UTF-8")));
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
            jobTraceView.setHtml("로그 파일을 찾을 수 없습니다. 로그 파일은 주기적으로 삭제되거나 저장 공간 관리를 위해서 임의로 삭제될 수 있습니다.");
        } catch (IOException e) {
            logger.warn(e.getMessage());
            jobTraceView.setHtml(String.format("오류가 발생하여 로그 파일을 읽을 수 없습니다. 원인: %s", e.getMessage()));
        }

        return jobTraceView;
    }

    @Transactional
    public void update(final Long jobId, final UpdateJobRequest updateJobRequest) throws IOException {
        //TODO validate token
        Job job = getJob(jobId);
        if (!authenticateJob(job, updateJobRequest.getToken())) {
            throw new InsufficientPrivilegeException();
        }

        jobProcessService.update(job, updateJobRequest.getState());

        if (StringUtils.hasText(updateJobRequest.getTrace())) {
            jobTraceService.trace(jobId, updateJobRequest.getTrace(), -1);
        }
    }


    @Transactional
    public void createJobs(Pipeline pipeline) {
        logger.debug("파이프라인 '#{}' 의 Job 정보를 데이터베이스에 저장합니다...", pipeline.getId());
        List<Job> jobs = buildJobs(pipeline);

        if (jobs.size() > 0) {
            jobRepository.save(jobs);
            logger.info("파이프라인 '{}' 에 설정된 '{}' 개의 Job 을 데이터베이스에 저장했습니다.", pipeline.getId(), jobs.size());
        }
        else {
            logger.warn("파이프라인 '{}' 에 설정된 Job 이 한 개도 없습니다. 파이프라인을 저장하지만 사용자에게는 설정 오류를 표시합니다.", pipeline.getId());
        }
    }

    /**
     * 전달받은 Job 의 정보를 복사해서 새로운 Job 을 만듭니다. Retry Action 을 처리하는 경우 사용합니다.
     *
     * @param job
     */
    @Transactional
    public void copyThenCreate(final Job job) {
        Job newJob = new Job();
        newJob.setPipeline(job.getPipeline());
        newJob.setName(job.getName());
        newJob.setWhen(job.getWhen());
        newJob.setStatus(PipelineStatus.CREATED);
        newJob.setStage(job.getStage());
        newJob.setStageIndex(job.getStageIndex());
        newJob.setArtifactsFile(job.getArtifactsFile());
        newJob.setCommands(job.getCommands());
        newJob.setOptions(job.getOptions());
        //필요 없음 newJob.setRunner(job.getRunner());

        newJob.setUpdatedAt(new Date());
        newJob.setCreatedAt(new Date());

        newJob = jobRepository.save(newJob);

        logger.debug("Job #{} ({}) 을 복사한 후 새로 저장했습니다. 새로 저장된 Job: #{}", job.getId(), job.getName(), newJob.getId());
    }

    /**
     * 실행 가능한 Job 목록을 반환합니다. <code>PENDING</code> 상태인 것들만 반환하는데, 이 Job 들 중 Runner 가 할당되지 않은 것들 혹은
     * 동일한 Runner 인 것만 돌려줍니다. TODO 나중에 Multiple Runner 를 관리할 때 다른 Runner 가 관리해도 되는 거 아닌가?
     *
     * @param runner
     * @return
     */
    @Transactional(readOnly = true)
    public List<Job> getRunnableJobs(final Runner runner) {

        //TODO failure 등 조건도 체크해야 함.
        List<Job> runnableJobs = jobRepository.findByStatus(PipelineStatus.PENDING);

        if (runnableJobs.isEmpty()) {
            return runnableJobs;
        }
        else {
            return runnableJobs.stream()
                    .filter(r -> r.getRunner() == null || r.getRunner().getId().longValue() == runner.getId().longValue())
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<Job> getJobs(final Pipeline pipeline) {

        return jobRepository.findByPipeline(pipeline);
    }

    @Transactional(readOnly = true)
    public List<Dependency> getDependencies(final Job job) {
        List<Job> dependsOnBuilds = dependsOnBuilds(job);
        if (dependsOnBuilds.isEmpty()) {
            return Collections.emptyList();
        }
        else {
            Map<String, Object> serializedOptions = job.getSerializedOptions();
            List<Dependency> dependencies = new ArrayList<>();

            if (!serializedOptions.containsKey("dependencies")) {
                for (Job j: dependsOnBuilds) {
                    if (j.hasArtifacts())
                        dependencies.add(convertJobToDependency(j));
                }

                return dependencies;
            }
            else {
                List<String> dependencyNames = (List)serializedOptions.get("dependencies");
                if (dependencyNames != null && !dependencyNames.isEmpty()) {
                    dependsOnBuilds.stream()
                            .filter(j -> dependencyNames.contains(j.getName()) && j.hasArtifacts())
                            .forEach(j -> dependencies.add(convertJobToDependency(j)));

                    return dependencies;
                }
            }

            return dependencies;
        }
    }

    private Dependency convertJobToDependency(final Job job) {
        Artifact artifact = Job.buildArtifact(job);
        if (artifact != null) {
            Dependency dependency = new Dependency();
            dependency.setId(job.getId());
            dependency.setName(job.getName());//TODO Job 이름이냐 Artifact 이름이냐?
            dependency.setToken("");
            dependency.setArtifactsFile(DependencyArtifactsFile.builder().fileName(job.getArtifactsFileName()).size(01).build());

            return dependency;
        }
        else {
            return null;
        }
    }

    /**
     * 현재 Job 이 있는 Stage 이전의 모든 Job 들을 가져옵니다.
     *
     * TODO naming?
     * @return
     */
    @Transactional(readOnly = true)
    public List<Job> dependsOnBuilds(final Job job) {
        List<Job> latestJobs = getLatestJobs(job.getPipeline());
        if (latestJobs.isEmpty()) {
            return latestJobs;
        }
        else {
            return latestJobs.stream().filter(j -> j.getStageIndex() < job.getStageIndex()).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<Job> getLatestJobs(final Pipeline pipeline) {
        List<Job> allJobs = this.getJobs(pipeline);
        List<Job> latestJobs = new ArrayList<>(
                this.getJobs(pipeline).stream().collect(groupingBy(j -> j.getName(), collectingAndThen(maxBy(comparingLong(j -> j.getId().longValue())), Optional::get))).values());

        //TODO trace?
        logger.debug("파이프라인 #{} 에 있는 총 Job 개수: {}, 걸러낸 최근 Job 개수: {}", pipeline.getId(), allJobs.size(), latestJobs.size());

        return latestJobs;
    }

    /**
     * FIXME Gitlab 에서는 기존에 해당 JobEntry Name 이 있는지 검사하는 코드를 사용하는데 생성할 때 이럴 일이 있는지 궁금하다.
     *
     def new_builds
     @new_builds ||= pipeline.config_builds_attributes.
     reject { |build| existing_build_names.include?(build[:name]) }
     end
     *
     * @return
     */
    private List<Job> buildJobs(Pipeline pipeline) {
        List<JobEntry> jobEntries = pipeline.getJobEntries();
        if (jobEntries.size() == 0)
            return Collections.emptyList();

        StagesEntry stagesEntry = pipeline.getCiConfig().getStagesEntry();

        List<Job> jobs = new ArrayList<>();
        Job job;
        for (JobEntry jobEntry: jobEntries) {
            job = new Job();
            job.setName(jobEntry.getName());
            job.setStage(jobEntry.getStageEntry().getName());
            //FIXME -1 인 경우는 에러를 보내야 하지 않나?
            job.setStageIndex(findIndexOfStages(stagesEntry.getStageEntries(), jobEntry.getStageEntry().getName()));
            job.setPipeline(pipeline);
            job.setCommands(jobEntry.getCommands());
            if (jobEntry.getArtifactsEntry() != null && !jobEntry.getArtifactsEntry().getPaths().isEmpty())
                job.setArtifactsFile(String.join(",", jobEntry.getArtifactsEntry().getPaths()));
            job.setWhen(jobEntry.getWhen());
            try {
                job.setOptions(jobEntry.toJson());
            } catch (JsonProcessingException e) {
                throw new JobCreationException(String.format("'%s' Job 을 생성할 수 없습니다. 원인: %s", job.getName(), e.getMessage()));
            }
            job.setStatus(PipelineStatus.CREATED);
            job.setCreatedAt(new Date());
            job.setUpdatedAt(new Date());

            jobs.add(job);
        }

        return jobs;
    }

    private Short findIndexOfStages(List<StageEntry> stageEntries, String stageName) {
        for (int i = 0; i < stageEntries.size(); i++) {
            if (stageEntries.get(i).getName().equals(stageName))
                return (short)i;
        }

        return (short)-1;
    }

    private boolean authenticateJob(final Job job, final String jobToken) {
        //TODO job 실행 권한 체크? 그런게 있나?

        return validateJobToken(job, jobToken);
    }

    private boolean validateJobToken(final Job job, final String jobToken) {
        if (StringUtils.hasText(job.getToken()) && StringUtils.hasText(jobToken)) {
            return job.getToken().equals(jobToken);
        }
        else {
            return false;
        }
    }

}
