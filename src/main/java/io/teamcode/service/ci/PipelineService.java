package io.teamcode.service.ci;

import io.teamcode.common.PaginationHelper;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.vcs.svn.SvnCommandHelper;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.common.vcs.svn.callback.SummaryLogMessage;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectMenuVisibility;
import io.teamcode.domain.entity.ci.*;
import io.teamcode.repository.PipelineRepository;
import io.teamcode.repository.RunnerRepository;
import io.teamcode.repository.UserRepository;
import io.teamcode.service.CannotCommitPipelineYamlException;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.service.vcs.svn.CommitService;
import io.teamcode.web.ui.view.ci.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;


/**
 * Created by chiang on 2017. 4. 8..
 */
@Service
@Transactional(readOnly = true)
public class PipelineService {

    private static final Logger logger = LoggerFactory.getLogger(PipelineService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    PipelineProcessService pipelineProcessService;

    @Autowired
    JobProcessService jobProcessService;

    @Autowired
    JobService jobService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    CommitService commitService;

    @Autowired
    RunnerRepository runnerRepository;

    public void commitPipelineYaml(String projectPath, String yaml, String message) {
        Project project = projectService.getByPath(projectPath);
        String relativePath = String.format("%s%s", projectPath, project.getResolvedPipelineConfigPath());
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), relativePath, TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

        try {
            logger.debug("저장소 주소 '{}' 에 새 Pipeline 설정 파일을 커밋합니다...", repositoryUri);
            SvnCommandHelper.doImport(tcConfig.getTempDir(), repositoryUri, yaml, message);
        } catch (IOException e) {
            logger.error("새 Pipeline 설정 파일을 커밋하던 중 오류가 발생했습니다.", e);

            throw new CannotCommitPipelineYamlException(e);
        }
    }

    public Pipeline getPipeline(final Long pipelineId) {
        Pipeline pipeline = pipelineRepository.findOne(pipelineId);

        if (pipeline == null)
            throw new ResourceNotFoundException(String.format("파이프라인 #%s 을 찾을 수 없습니다.", pipelineId));

        pipeline.setJobs(jobService.getLatestJobs(pipeline));

        return pipeline;
    }

    public boolean hasPipelineYaml(String projectPath) {
        Project project = projectService.getByPath(projectPath);
        String relativePath = String.format("%s%s", projectPath, project.getResolvedPipelineConfigPath());
        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), relativePath, TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

        logger.debug("파이프라인 설정 파일이 있는지 확인합니다. 파일 위치: {}", repositoryUri);

        return SvnCommandHelper.exist(repositoryUri);
    }

    public PipelinesView getPipelinesView(final String groupPath,
                                          final String projectPath,
                                          final String scope,
                                          final int page,
                                          final HttpServletResponse httpServletResponse) {
        Project project = projectService.getByPath(projectPath);

        PipelinesView pipelinesView = new PipelinesView();

        PipelinesCountView pipelinesCountView = new PipelinesCountView();
        pipelinesCountView.setAll(pipelineRepository.countByProject(project));
        pipelinesCountView.setRunning(pipelineRepository.countByProjectAndStatus(project, PipelineStatus.RUNNING));
        pipelinesCountView.setPending(pipelineRepository.countByProjectAndStatus(project, PipelineStatus.PENDING));
        pipelinesCountView.setFinished(pipelineRepository.countByProjectAndStatusIn(project,
                Arrays.asList(PipelineStatus.SUCCESS, PipelineStatus.SUCCESS_WITH_WARNINGS, PipelineStatus.CANCELED, PipelineStatus.FAILED)));
        pipelinesView.setCount(pipelinesCountView);

        //TODO scoping...
        PageRequest request =
                new PageRequest(page - 1, 10, Sort.Direction.DESC, "id");

        Page<Pipeline> pipelines = getPipelines(project, scope, request);

        PipelineView pipelineView;
        for (Pipeline pipeline: pipelines) {
            logger.debug("pipeline revision: {}, started at: {}", pipeline.getCommitRevision(), pipeline.getStartedAt());
            pipeline.setJobs(jobService.getLatestJobs(pipeline));

            pipelineView = new PipelineView();

            pipelineView.setId(pipeline.getId());
            pipelineView.setPath(String.format("/%s/%s/pipelines/%s", groupPath, projectPath, pipeline.getId()));
            pipelineView.setCommit(commitService.getCommit(projectPath, pipeline.getCommitRevision()));
            pipelineView.setYamlErrors(pipeline.getYamlErrors());
            pipelineView.setCreatedAt(pipeline.getCreatedAt());
            pipelineView.setUpdatedAt(pipeline.getUpdatedAt());

            pipelineView.setDetails(buildPipelineDetailsView(groupPath, projectPath, pipeline, false));
            pipelineView.setFlags(buildPipelineFlags(pipeline));
            if (pipelineView.getFlags().isCancelable()) {
                pipelineView.setCancelPath(String.format("/%s/%s/pipelines/%s/cancel", groupPath, projectPath, pipeline.getId()));
            }
            if (pipelineView.getFlags().isRetryable()) {
                pipelineView.setRetryPath(String.format("/%s/%s/pipelines/%s/retry", groupPath, projectPath, pipeline.getId()));
            }

            pipelinesView.addPipelineView(pipelineView);
        }

        /*Page<PipelineView> pipelineViewPage = pipelines.map(pipeline -> {
            logger.debug("pipeline revision: {}, started at: {}", pipeline.getCommitRevision(), pipeline.getStartedAt());
            pipeline.setJobs(jobService.getLatestJobs(pipeline));

            PipelineView pipelineView = new PipelineView();

            pipelineView.setId(pipeline.getId());
            pipelineView.setPath(String.format("/%s/%s/pipelines/%s", groupPath, projectPath, pipeline.getId()));
            pipelineView.setCommit(commitService.getCommit(projectPath, pipeline.getCommitRevision()));
            pipelineView.setYamlErrors(pipeline.getYamlErrors());
            pipelineView.setCreatedAt(pipeline.getCreatedAt());
            pipelineView.setUpdatedAt(pipeline.getUpdatedAt());

            pipelineView.setDetails(buildPipelineDetailsView(groupPath, projectPath, pipeline, false));
            pipelineView.setFlags(buildPipelineFlags(pipeline));
            if (pipelineView.getFlags().isCancelable()) {
                pipelineView.setCancelPath(String.format("/%s/%s/pipelines/%s/cancel", groupPath, projectPath, pipeline.getId()));
            }
            if (pipelineView.getFlags().isRetryable()) {
                pipelineView.setRetryPath(String.format("/%s/%s/pipelines/%s/retry", groupPath, projectPath, pipeline.getId()));
            }

            return pipelineView;
        });*/

        //pipelinesView.setPipelinesPage(pipelineViewPage);

        PaginationHelper.putPaginationHeader(httpServletResponse, pipelines);

        return pipelinesView;
    }

    public PipelineView getPipelineView(final String groupPath, final String projectPath, final Long pipelineId) {
        Pipeline pipeline = getPipeline(pipelineId);

        PipelineView pipelineView = new PipelineView();

        pipelineView.setId(pipeline.getId());
        pipelineView.setPath(String.format("/%s/%s/pipelines/%s", groupPath, projectPath, pipeline.getId()));
        pipelineView.setCommit(commitService.getCommit(projectPath, pipeline.getCommitRevision()));
        pipelineView.setYamlErrors(pipeline.getYamlErrors());
        pipelineView.setCreatedAt(pipeline.getCreatedAt());
        pipelineView.setUpdatedAt(pipeline.getUpdatedAt());

        pipelineView.setDetails(buildPipelineDetailsView(groupPath, projectPath, pipeline, true));

        return pipelineView;
    }

    public PipelineStageDetailsView getPipelineStageDetailsView(final String groupPath,
                                                                final String projectPath,
                                                                final Long pipelineId, final String stage) {
        Pipeline pipeline = getPipeline(pipelineId);

        List<Job> jobsInStage = pipeline.getJobsByStageName(stage);

        PipelineStageDetailsView pipelineStageDetailsView = new PipelineStageDetailsView();

        String jobActionPath = null;
        PipelineStatus resolvedJobStatus;
        for (Job job: jobsInStage) {
            if (job.getStatus().isHasAction())
                jobActionPath = job.getStatus().getJobAction().name().toLowerCase();

            resolvedJobStatus = job.getStatus();

            pipelineStageDetailsView.addPipelineJobView(
                    PipelineJobView.builder()
                            .id(job.getId())
                            .name(job.getName())
                            .path(String.format("/%s/%s/jobs/%s", groupPath, projectPath, job.getId()))
                            .actionPath(String.format("/%s/%s/jobs/%s/%s", groupPath, projectPath, job.getId(), jobActionPath))
                            .status(JobStatusView.builder().jobStatus(resolvedJobStatus).build()).build());
        }

        return pipelineStageDetailsView;
    }

    private boolean getStuckStatus(final Pipeline pipeline) {

        boolean online = false;
        Iterable<Runner> runners = runnerRepository.findAll();
        for (Runner runner: runners)
            if (runner.isOnline())
                online = true;

        return pipeline.getResolvedStatus() == PipelineStatus.PENDING && !online;
    }

    /**
     * Stage 상태의 경우 이전 상태 값에 영향을 받습니다. 예를 들어 현재 Running 중인 Stage 이후는 모두 Created 상태로 표시합니다.
     * 만약 현재 Pipeline 이 Failed 라면 Created 상태인 Stage 는 모두 Skipped 가 됩니다.
     *
     *
     * @param groupPath
     * @param projectPath
     * @param pipeline
     * @param fetchJobs Stage 에 Job 을 모두 불러서 할당할 것인지 여부. 파이프라인 상세 페이지에서만 사용합니다.
     * @return
     */
    private PipelineDetailsView buildPipelineDetailsView(final String groupPath, final String projectPath, final Pipeline pipeline, boolean fetchJobs) {
        PipelineDetailsView pipelineDetailsView = new PipelineDetailsView();

        PipelineStageView pipelineStageView;
        for(Stage stage: pipeline.getStages(fetchJobs)) {
            pipelineStageView = new PipelineStageView();
            pipelineStageView.setName(stage.getName());
            pipelineStageView.setStatus(PipelineStatusView.builder().pipelineStatus(stage.getStatus()).build());
            pipelineStageView.setPath(String.format("/%s/%s/pipelines/%s/stage.json?stage=%s", groupPath, projectPath, pipeline.getId(), stage.getName()));

            if (fetchJobs) {
                for (Job job: stage.getJobs()) {

                    pipelineStageView.addPipelineJobView(
                            PipelineJobView.builder()
                                    .id(job.getId())
                                    .name(job.getName())
                                    .duration(job.resolveDuration())
                                    .status(JobStatusView.builder().jobStatus(job.getStatus()).build())
                                    .path(String.format("/%s/%s/jobs/%s", groupPath, projectPath, job.getId()))
                                    .build());
                }
            }

            pipelineDetailsView.addStage(pipelineStageView);
        }

        pipelineDetailsView.setStatus(
                PipelineStatusView.builder()
                        .pipelineStatus(pipeline.getStatus())
                        .hasDetails(true)
                        .detailsPath(new StringBuilder("/").append(groupPath).append("/").append(projectPath).append("/pipelines/").append(pipeline.getId()).toString())
                        .build());

        pipelineDetailsView.setDuration(pipeline.getDuration());
        pipelineDetailsView.setFinishedAt(pipeline.getFinishedAt());

        return pipelineDetailsView;
    }

    private PipelineFlags buildPipelineFlags(final Pipeline pipeline) {
        PipelineFlags flags = new PipelineFlags();
        flags.setYamlErrors(pipeline.hasYamlErrors());
        flags.setStuck(getStuckStatus(pipeline));
        flags.setCancelable(pipeline.isCancelable());
        flags.setRetryable(pipeline.isRetryable());

        return flags;
    }

    public Page<Pipeline> getPipelines(final Project project, final String scope, final PageRequest request) {

        if ("running".equals(scope)) {
            return pipelineRepository.findByProjectAndStatus(project, PipelineStatus.RUNNING, request);
        }
        else if ("pending".equals(scope)) {
            return pipelineRepository.findByProjectAndStatus(project, PipelineStatus.PENDING, request);
        }
        else if ("finished".equals(scope)) {
            return pipelineRepository.findByProjectAndStatusIn(
                    project,
                    Arrays.asList(PipelineStatus.SUCCESS, PipelineStatus.FAILED, PipelineStatus.CANCELED),
                    request);
        }
        else {
            return pipelineRepository.findByProject(project, request);
        }
    }

    @Transactional
    public void createPipeline(final String projectPath, final long revisionNumber, SummaryLogMessage summaryLogMessage, boolean isSkip) {
        Project project = projectService.getByPath(projectPath);

        if (project.getPipelineVisibility() == ProjectMenuVisibility.ENABLED) {
            if (!hasPipelineYaml(project.getPath())) {
                logger.warn("'{}' 프로젝트에 대한 파이프라인을 생성하는 요청을 받았으나 '{}' 파일을 없어 요청을 무시합니다.", project.getName(), TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

                return;
            }

            logger.info("프로젝트 '{}' 의 파이프라인을 생성합니다...", project.getName());

            Pipeline pipeline = new Pipeline();
            pipeline.setProject(project);
            pipeline.setCommitRevision(revisionNumber);
            pipeline.setCreatedBy(userRepository.findByName(summaryLogMessage.getAuthor()));
            pipeline.setCreatedAt(new Date());

            if (isSkip) {
                pipeline.setStatus(PipelineStatus.SKIPPED);
                logger.debug("이 커밋에 skip-ci 설정이 되어 있어 CI 파이프라인 실행을 하지 않습니다.");
            }
            else
                pipeline.setStatus(PipelineStatus.CREATED);

            if (!isSkip) {
                String relativePath = String.format("%s%s", projectPath, project.getResolvedPipelineConfigPath());
                String ymlPath = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootDir().getAbsolutePath(), relativePath, TeamcodeConstants.DEFAULT_PIPELINE_YAML_FILE_NAME);

                pipeline.processConfig(SvnCommandHelper.getContent(ymlPath));
                if (pipeline.hasYamlErrors()) {
                    pipeline.setYamlErrors(pipeline.getYamlErrors());
                    pipeline.setStatus(PipelineStatus.FAILED);
                }
            }

            Pipeline savedPipeline = pipelineRepository.save(pipeline);

            if (!isSkip && !savedPipeline.hasYamlErrors()) {
                jobService.createJobs(savedPipeline);

                //FIXME OneToMany not working
                savedPipeline.setJobs(jobService.getJobs(savedPipeline));
                pipelineProcessService.process(savedPipeline);
            }
        }
        else {
            logger.debug("프로젝트 '{}' 의 파이프라인 메뉴가 '{}' 상태입니다. 파이프리안 생성 요청을 무시합니다.", project.getName(), project.getPipelineVisibility());
        }
    }


    /*
    Gitlab::OptimisticLocking.retry_lock(cancelable_statuses) do |cancelable|
        cancelable.find_each do |job|
          yield(job) if block_given?
          job.cancel
        end
      end

     */

    /**
     * 이 요청을 받으면 Pipeline 상태를 변경하지 않고 Job 을 변경해서 전체 상태를 Pipleline 이 파악할 수 있도록 한다.
     *
     * @param pipelineId
     */
    @Transactional
    public void cancelRunning(final Long pipelineId) {
        Pipeline pipeline = getPipeline(pipelineId);
        logger.debug("파이프라인 #{} 실행을 취소합니다...", pipeline.getId());

        long cancelableCount = pipeline.getJobs().stream().filter(j -> j.canCanceled()).count();
        logger.debug("파이프라인 #{} 에서 취소할 Job 이 {}개 있습니다", pipelineId, cancelableCount);

        for (Job job: pipeline.getCanCancelableJobs())
            jobProcessService.cancel(job);

        logger.debug("파이프라인 #{} 실행을 취소했습니다.", pipeline.getId());
    }

    @Transactional
    public void retry(final Long pipelineId) {
        Pipeline pipeline = getPipeline(pipelineId);
        logger.debug("파이프라인 #{} 을 다시 실행합니다...", pipeline.getId());

        List<Job> retryableJobs = pipeline.getRetryableJobs();
        logger.debug("재실행할 수 있는 Job 이 총 {} 개 있습니다.", retryableJobs.size());
        for (Job job: retryableJobs) {
            jobService.copyThenCreate(job);
        }

        //Job 정보가 바뀌었으므로 요청받은 Pipeline 에 새로운 Job 을 할당하게 원래 돌리던 Process 를 가동시킵니다.
        pipeline.setJobs(jobService.getLatestJobs(pipeline));
        pipelineProcessService.process(pipeline);

        /**
         GitLab Way...

         * pipeline.builds.latest.skipped.find_each do |skipped|
         retry_optimistic_lock(skipped) { |build| build.process }
         end

         MergeRequests::AddTodoWhenBuildFailsService
         .new(project, current_user)
         .close_all(pipeline)

         pipeline.process!

         */
    }


}
