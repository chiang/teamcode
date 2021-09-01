package io.teamcode.service.ci;

import io.teamcode.common.ci.CiConfigProcessor;
import io.teamcode.common.ci.JobHelper;
import io.teamcode.common.ci.RunnerHelper;
import io.teamcode.common.ci.RunnerRequest;
import io.teamcode.common.ci.config.CiConfig;
import io.teamcode.common.vcs.svn.SvnCommandHelper;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ApplicationSetting;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ci.CiVariable;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.JobWhen;
import io.teamcode.domain.entity.ci.Runner;
import io.teamcode.repository.RunnerRepository;
import io.teamcode.service.ApplicationService;
import io.teamcode.service.ResourceNotFoundException;
import io.teamcode.web.api.model.ci.*;
import io.teamcode.web.ui.view.ci.RunnersView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Runner 가 요청하는 정보를 처리하거나 Runner 에 관련된 정보를 처리하는 서비스입니다.
 *
 * Created by chiang on 2017. 4. 11..
 */
@Service
@Transactional(readOnly = true)
public class RunnerService {

    private static final Logger logger = LoggerFactory.getLogger(RunnerService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    PipelineService pipelineService;

    @Autowired
    RunnerRepository runnerRepository;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    JobService jobService;

    @Autowired
    JobProcessService jobProcessService;

    @Autowired
    CiVariableService ciVariableService;

    public RunnersView getRunnersView() {
        RunnersView runnersView = new RunnersView();
        runnersView.setCurrentToken(applicationService.getApplicationSetting().getRunnersRegistrationToken());
        runnersView.setRunners(runnerRepository.findAll());

        return runnersView;
    }

    @Transactional
    public void resetRunnerToken() {
        ApplicationSetting applicationSetting = applicationService.getApplicationSetting();

        applicationSetting.setRunnersRegistrationToken(generateToken());
        applicationService.updateApplicationSetting(applicationSetting);

        logger.info("Runners 등록 토큰을 재설정했습니다.");
    }

    //FIXME 동기화 처리? 우리는 처음에는 Single Runner 만 가지고 있으므로 지금은 고려할 부분이 아니다.x
    @Transactional
    public JobResponse pickThenRunJobs(RunnerRequest runnerRequest) {
        //this.verifyRunnerThenIfNotRegisteredThenRegister(runnerRequest.getToken(), runnerRequest.getInfo());

        Runner runner = runnerRepository.findByToken(runnerRequest.getToken());
        if (runner == null || runner.getActive() == Boolean.FALSE) {
            logger.warn("Runner (token={}) 을 찾을 수 없습니다.", runnerRequest.getToken());
            throw new ResourceNotFoundException("Runner 를 찾을 수 없습니다.");
        }
        updateRunnerInfo(runner, runnerRequest);

        //FIXME 만약 Runner 가 중지된 상태에서 다시 실행을 하면 Runnable Job 이 Pending 상태인 것만 조회하므로 Running 상태인 것을
        //확인하지 않는다.
        List<Job> runnableJobs = jobService.getRunnableJobs(runner);
        /*logger.debug("총 {} 개의 실행 가능한 Job 이 있습니다. 순차적으로 실행합니다...", runnableJobs.size());
        for(Job job: runnableJobs) {
            job.setRunner(runner);
            jobProcessService.run(job);
        }*/

        if (runnableJobs.size() == 0) {
            logger.debug("실행할 Job 이 하나도 없습니다.");
            throw new ResourceNotFoundException("Runnable jobs not exist");
        }

        Job oneJob = runnableJobs.stream().findFirst().get();
        logger.debug("총 {} 개의 실행 가능한 Job 이 있습니다. 이 중에서 한 개 ({}) 를 꺼내 실행하고 Runner 쪽에 돌려줍니다.", runnableJobs.size(), oneJob.getName());

        oneJob.setRunner(runner);
        oneJob.setToken(JobHelper.generateJobToken(oneJob));

        jobProcessService.run(oneJob);

        //Build JobResponse...........
        Project project = oneJob.getPipeline().getProject();
        //TODO 검토 필요. 미리 Running 상태로 두었다가 혹시 데이터가 제대로 전송되지 않으면? 그럴 일이 있나?
        JobResponse jobResponse = new JobResponse();
        jobResponse.setJob(oneJob);
        jobResponse.setPipelineId(oneJob.getPipeline().getId());
        jobResponse.setId(oneJob.getId());
        jobResponse.setToken(oneJob.getToken());
        jobResponse.setJobInfo(JobInfo.builder().name(oneJob.getName()).projectId(project.getId()).projectName(project.getName()).stage(oneJob.getStage()).build());

        String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryHostRootPath(), project.getPath(), project.getResolvedPipelineConfigPath());
        jobResponse.setRepositoryInfo(RepositoryInfo.builder().url(repositoryUri).revision(oneJob.getPipeline().getCommitRevision().toString()).build());

        if (StringUtils.hasText(oneJob.getCommands()))
            jobResponse.addStep(Step.builder().name("script").scripts(Arrays.asList(oneJob.getCommands().split("\n"))).when(JobWhen.ON_SUCCESS).timeout(project.getBuildTimeout()).build());

        jobResponse.setArtifact(Job.buildArtifact(oneJob));
        jobResponse.setDependencies(jobService.getDependencies(oneJob));

        Map<String, Object> options = oneJob.getSerializedOptions();
        if (!options.isEmpty() && options.containsKey("after-script")) {
            String afterScript = (String)options.get("after-script");
            if (StringUtils.hasText(afterScript)) {
                Step step = Step.builder()
                        .name("after-script")
                        .scripts(Arrays.asList(afterScript.split("\n")))
                        .timeout(project.getBuildTimeout())
                        .when(JobWhen.ALWAYS)
                        .allowFailure(true).build();

                jobResponse.addStep(step);
            }
        }

        setupVariables(jobResponse, project.getPath());
        setupPipelineYaml(jobResponse, project);

        return jobResponse;
    }

    private void setupVariables(final JobResponse jobResponse, final String projectPath) {
        List<CiVariable> ciVariables = ciVariableService.getCiVariables(projectPath);
        List<Variable> variables = ciVariables.stream().map(v -> Variable.builder().name(v.getName()).value(v.getValue()).build()).collect(Collectors.toList());

        jobResponse.setVariables(variables);
    }

    private void setupPipelineYaml(final JobResponse jobResponse, final Project project) {
        if (pipelineService.hasPipelineYaml(project.getPath())) {
            CiConfigProcessor processor = new CiConfigProcessor();
            processor.process(SvnCommandHelper.getPipelineYmlContent(project, tcConfig.getRepositoryRootDir()));
            //processor.process(SvnCommandHelper.getPipelineYamlContent(tcConfig.getRepositoryRootPath(), projectPath));

            CiConfig ciConfig = processor.getCiConfig();
            if (StringUtils.hasText(ciConfig.getImage())) {
                jobResponse.setImage(ciConfig.getImage());
            }
        }
    }

    @Transactional
    public void verifyRunnerThenIfNotRegisteredThenRegister(final RunnerRequest runnerRequest) {
        Runner registeredRunner = runnerRepository.findByToken(runnerRequest.getToken());
        if (registeredRunner != null) {
            updateRunnerInfo(registeredRunner, runnerRequest);

            logger.info("Runner #{} 정보를 업데이트했습니다.", registeredRunner.getId());

            cancelActiveJobsWhenRunnerRestarted(registeredRunner);
        }
        else {
            ApplicationSetting applicationSetting = applicationService.getApplicationSetting();

            if (applicationSetting.getRunnersRegistrationToken().equals(runnerRequest.getToken())) {
                Runner newRunner = new Runner();
                newRunner.setToken(applicationSetting.getRunnersRegistrationToken());
                newRunner.setName(runnerRequest.getInfo().getName());
                newRunner.setActive(Boolean.TRUE);
                newRunner.setPlatform(runnerRequest.getInfo().getPlatform());
                newRunner.setArchitecture(runnerRequest.getInfo().getArchitecture());
                newRunner.setVersion(runnerRequest.getInfo().getVersion());
                newRunner.setRevision(runnerRequest.getInfo().getRevision());
                newRunner.setContactedAt(new Date());
                newRunner.setCreatedAt(new Date());
                newRunner.setUpdatedAt(new Date());
                newRunner = runnerRepository.save(newRunner);


                //reset runner registration token
                applicationSetting.setRunnersRegistrationToken(generateToken());
                applicationService.updateApplicationSetting(applicationSetting);

                logger.info("Runner #{} 를 새로 등록했습니다.", newRunner.getId());
            }
            else {
                //TODO 적절한 에러 코드 전달 (HTTP Status Code 와 함께?)
                throw new IllegalArgumentException("Runner 가 전달한 Token 이 유효하지 않습니다.");
            }
        }
    }

    private void updateRunnerInfo(Runner runner, final RunnerRequest runnerRequest) {
        if (!canUpdateRunnerInfo(runner))
            return;

        logger.debug("Runner #{} 정보를 업데이트합니다...", runner.getId());
        runner.setActive(Boolean.TRUE);//홋시 몰라 항상 True 로 처리
        runner.setName(runnerRequest.getInfo().getName());
        runner.setPlatform(runnerRequest.getInfo().getPlatform());
        runner.setArchitecture(runnerRequest.getInfo().getArchitecture());
        runner.setVersion(runnerRequest.getInfo().getVersion());
        runner.setRevision(runnerRequest.getInfo().getRevision());
        runner.setContactedAt(new Date());
        runner.setUpdatedAt(new Date());

        runnerRepository.save(runner);
    }

    private void cancelActiveJobsWhenRunnerRestarted(final Runner runner) {
        logger.info("이전에 Runner 가 실행 중인 (혹은 실행할) Active Job 이 있는지 확인 후에 해당 Job 들을 취소합니다...");
        long canceledJobs = jobService.cancelActiveJobs(runner);
        if (canceledJobs == 0) {
            logger.info("이전에 Runner 가 실행 중인 (혹은 실행할) Job 이 하나도 없습니다.");
        }
    }

    /**
     * 매번 요청이 올 때마다 Update 를 수행하게 구성했으나 너무 자주 DB Update 를 하지 않도록 제어합니다.
     *
     *
     GitLab Way
     # Use a random threshold to prevent beating DB updates.
     # It generates a distribution between [40m, 80m].
     #
     # UPDATE_RUNNER_EVERY = 10 * 60
     contacted_at_max_age = UPDATE_RUNNER_EVERY + Random.rand(UPDATE_RUNNER_EVERY)

     current_runner.contacted_at.nil? ||
     (Time.now - current_runner.contacted_at) >= contacted_at_max_age
     *
     * @return
     */
    private boolean canUpdateRunnerInfo(final Runner runner) {
        if (runner.getContactedAt() == null)
            return true;

        //TODO
        return true;
    }

    /**
     * 중복을 막기 위해서 계속 확인하나 이게 무한 루프에 걸릴 확률은 거의 없다.
     *
     * @return
     */
    private String generateToken() {
        String token = RunnerHelper.generateRunnersRegistrationToken();
        if (runnerRepository.findByToken(token) != null) {
            return generateToken();
        }

        return token;
    }
}
