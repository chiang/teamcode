package io.teamcode.service.ci;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.common.statemachine.OnTransitionEndInPipeline;
import io.teamcode.common.statemachine.OnTransitionStartInPipeline;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.JobWhen;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import io.teamcode.repository.PipelineRepository;
import io.teamcode.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 4. 18..
 */
//만약 Factory 에서 getStateMachine 를 호출할 때 ID 를 선언하면 여기에도 ID 를 꼭 넣어야 @OnTransition 과 같은  Annotation 이 동작합니다.
@WithStateMachine(id = "pipelineStateMachine")
@Service
public class PipelineProcessService {

    private static final Logger logger = LoggerFactory.getLogger(PipelineProcessService.class);

    @Autowired
    JobProcessService jobProcessService;

    @Autowired
    JobService jobService;

    @Autowired
    @Qualifier("pipelineStateMachineFactory")
    StateMachineFactory<PipelineStatus, PipelineEvent> stateMachineFactory;

    @Autowired
    PipelineService pipelineService;

    @Autowired
    PipelineRepository pipelineRepository;

    /**
     * Pipeline Entity 가 아니라 Entity ID 만 받아서 처리합니다. 비동기로 실행되니까 Hibernate Session 을 잃을 수 있습니다.
     *
     * @param pipelineId
     */
    //@Async
    public void processAsync(final Long pipelineId) {
        try {
            Pipeline pipeline = pipelineService.getPipeline(pipelineId);
            process(pipeline);

        } catch (ResourceNotFoundException e) {
            logger.warn("비동기로 Pipeline 을 실행하도록 요청 받았으나 Pipeline #{} 을 찾을 수 없어 요청을 처리할 수 없습니다.", pipelineId);
        }
    }

    /**
     * 이 Method 는 다른 Job 을 완료처리한 다음에 호출이 될 수 있는데 이미 PENDING 상태이거나 처리 중인 것은 가져오지 않습니다. CREATED 상태인 Job 만
     * 가져오기 때문에 뭔가 중복 실행이 될 일은 없습니다.
     *
     * @param pipeline
     */
    @Transactional
    public void process(Pipeline pipeline) {
        logger.debug("파이프라인 #{} 을 실행합니다...", pipeline.getId());

        List<Job> createdJobs = pipeline.getCreatedJobs();
        if (createdJobs.isEmpty()) {
            //여기를 탈 일은 없다.
            logger.warn("파이프라인 #{} 에서 실행할 Job 이 하나도 없습니다. 요청을 처리하지 않습니다.", pipeline.getId());
        }
        else {
            createdJobs.sort(Comparator.comparing(Job::getStageIndex));
            List<Short> stageIndexes = createdJobs.stream().map(j -> j.getStageIndex()).distinct().collect(Collectors.toList());

            for (Short stageIndex: stageIndexes) {
                processStage(pipeline, stageIndex);
            }
        }

        //요청을 처리한 후 결과 상태를 현재 파이프라인에 반영합니다.
        updateStatus(pipeline);

        /*//TODO 만약 예전에 실행된 적이 있는 파이프라인이라면 어떻게 되는가?
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = stateMachineFactory.getStateMachine("pipelineStateMachine");

        Message<PipelineEvent> message = MessageBuilder
                .withPayload(PipelineEvent.RUN)
                .setHeader("pipeline", pipeline)
                .setHeader("pipelineId", pipeline.getId())
                .build();
        stateMachine.sendEvent(message);
        logger.debug("Current pipeline was started with state: {}, startedAt: {}", stateMachine.getState().getId(), pipeline.getStartedAt());


        pipeline.setStatus(pipeline.resolveStatus());
        pipelineRepository.save(pipeline);*/
    }

    /**
     * 최근 빌드 상태를 조회해서 이 값을 Pipeline 에 설정합니다. FIXME Parallel 하게 요청하는 것에서 마지막이 있는가???
     *
     * @param pipeline
     */
    @Transactional
    public void updateStatus(final Pipeline pipeline) {
        //TODO synchronization

        if (pipeline.getJobs() == null) {
            pipeline.setJobs(jobService.getLatestJobs(pipeline));
        }

        PipelineStatus resolvedStatus = pipeline.getResolvedStatus();
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(pipeline);
        logger.debug("Resolved current pipeline #{} status (from database): {}, This pipeline state machine's state: {}", pipeline.getId(), resolvedStatus, stateMachine.getState().getId());

        switch(resolvedStatus) {
            case PENDING:
                stateMachine.sendEvent(PipelineEvent.ENQUEUE);
                break;

            case CANCELED:
                stateMachine.sendEvent(PipelineEvent.CANCEL);
                break;

            case RUNNING:
                stateMachine.sendEvent(PipelineEvent.RUN);
                break;

            case SUCCESS:
                stateMachine.sendEvent(PipelineEvent.SUCCESS);
                break;

            case FAILED:
                stateMachine.sendEvent(PipelineEvent.DROP);
                break;
        }
    }

    /**
     * Stage 에 있는 모든 Job 을 실행합니다 (조건에 따라). 만약 이전 Stage 가 완료된 상태가 아니라면 실행하지 않습니다.
     *
     * @param pipeline
     * @param stageIndex 현재 실행할 Stage Index. 값은 0 부터 시작합니다.
     */
    private void processStage(final Pipeline pipeline, final Short stageIndex) {
        logger.debug("파이프라인 #{} 의 스테이지 #{} 을 실행합니다...", pipeline.getId(), stageIndex);

        List<Job> previousStageJobs;
        if (stageIndex == 0)
            previousStageJobs = Collections.emptyList();
        else {
            previousStageJobs =
                    pipeline.getJobs().stream()
                            .filter(j -> j.getStageIndex().shortValue() < stageIndex && j.getStageIndex().shortValue() > (stageIndex - 2))
                            .collect(Collectors.toList());
        }

        PipelineStatus previousStageStatus;
        if (previousStageJobs.size() > 0) {
            previousStageStatus = Pipeline.resolveStatus(previousStageJobs);
        }
        else {
            //이전 Stage 에 Job 이 한 개도 없다면 그냥 성공한 셈으로
            previousStageStatus = PipelineStatus.SUCCESS;
        }

        logger.debug("파이프라인 #{} 의 현재 스테이지는 #{} 입니다. 이전 스테이지의 상태는 {} 입니다.", pipeline.getId(), stageIndex, previousStageStatus);

        if (previousStageStatus == PipelineStatus.MANUAL) {
            logger.debug("이전 Job 이 수동으로 실행하게 설정되어 있어 현재 Stage 를 실행하지 않습니다.");
            return;
        }
        else if (previousStageStatus.isCompletedStatus()) {
            List<Job> createdJobsInCurrentStage
                    = pipeline.getJobs().stream().filter(j -> j.getStageIndex().shortValue() == stageIndex).collect(Collectors.toList());

            logger.debug("총 '{}' 개 실행할 Job 이 있습니다. Job 을 실행합니다.", createdJobsInCurrentStage.size());
            for (Job job: createdJobsInCurrentStage) {
                processJob(job, previousStageStatus);
            }
        }
        else {
            logger.debug("파이프라인 #{} 의 스테이지 #{} 의 이전 Stage 가 완료되지 않아 요청받은 Stage 의 Job 을 실행하지 않습니다.", pipeline.getId(), stageIndex);
        }
    }

    private void processJob(final Job job, final PipelineStatus previousStageStatus) {
        List<PipelineStatus> validStatuses = new ArrayList<>();
        logger.debug("Processing a job #{} ({}) when: {}, previous stage status: {}", job.getId(), job.getName(), job.getWhen(), previousStageStatus);

        switch(job.getWhen()) {
            case ON_SUCCESS:
                validStatuses.add(PipelineStatus.SUCCESS);
                validStatuses.add(PipelineStatus.SKIPPED);
                break;

            case ON_FAILURE:
                validStatuses.add(PipelineStatus.FAILED);
                break;

            case ALWAYS:
                validStatuses.add(PipelineStatus.SUCCESS);
                validStatuses.add(PipelineStatus.FAILED);
                validStatuses.add(PipelineStatus.SKIPPED);
                break;

            case MANUAL:
                //TODO 현재 Stage 가 Canceled 인 경우 Manual Job 은 Canceled 가 되는 것이 맞다. 하지만...

                if (previousStageStatus != PipelineStatus.FAILED) {
                    logger.debug("현재 Job #{} ({}) 은 Manual Job 입니다. 상태를 Manual 로 설정합니다...", job.getId(), job.getName());
                    jobService.updateStatus(job, PipelineStatus.MANUAL);
                    return;
                }
        }

        //현재 Job 의 조건이 위의 조건에 부합하는지를 확인한 후 실행
        //즉, Job 은 이전 Stage 의 Job Status 에 따라서 실행할지 말지를 결정해야 하는데 그것은 또한 현재 Job 의 When 조건도 따지게 됩니다.
        //그래서 해당 조건에 맞으면 실행하는 형태입니다.
        if (validStatuses.contains(previousStageStatus)) {
            jobProcessService.enqueue(job);//TODO actionize?
        }
        else {
            //위 조건에 맞지 않는다는 것은, 이전 Stage 상태가 취소되었거나 오류가 발생해서 현재 Stage 를 실행하지 못한다는 의미입니다.
            //그래서 요청받은 Stage 를 Skipped 상태로 처리합니다.
            //Pipeline 을 Cancel 하는 경우, 수행 중이던 Stage 이후의 Stage 내 Job 들은 여기서 모두 SKIPPED 로 설정됩니다.
            //그래서 SKIPPED 인 것을 다시 Cancel 하려면 Transition(SKIPPED --> CANCELED) 이 정의되어 있어야 하는데
            //GitLab 에서는 이게 정의되어 있지 않습니다. Framework 의 차이로 인한 것 같으며 그래서 우리는 해당 Transition 을 정의해
            //두었습니다.

            /**
             * <ul>
             *     <li>이전 Stage 상태가 FAILED 이고 현재 Job 이 Manual 인 경우 Skip 으로 처리가 됩니다.</li>
             * </ul>
             */
            logger.debug("현재 Job #{} ({}) 이 속한 Stage #{} ({}) 가 이 실행할 조건에 맞지 않아 현재 Job 을 Skip 처리합니다.",
                    job.getId(), job.getName(), job.getStageIndex(), job.getStage());
            jobProcessService.skip(job);
        }
    }

    public void beforePending(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        logger.debug("Created #{} pipeline... ", stateContext.getMessageHeaders().get("pipelineId"));

    }

    @OnTransitionStartInPipeline(source = {PipelineStatus.CREATED, PipelineStatus.PENDING}, target = PipelineStatus.RUNNING)
    public void beforeRunning(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Pipeline pipeline = (Pipeline)stateContext.getExtendedState().getVariables().get("pipeline");
        logger.debug("파이프라인 #{} on before running.", pipeline.getId());

        pipeline.setStartedAt(new Date());// Transaction 내부에 있으므로 자동으로 이 값이 저장이 됩니다.
    }

    @OnTransitionStartInPipeline(target = {PipelineStatus.SUCCESS, PipelineStatus.FAILED, PipelineStatus.CANCELED})
    public void beforeFinished(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Pipeline pipeline = (Pipeline)stateContext.getExtendedState().getVariables().get("pipeline");
        logger.debug("파이프라인 #{} on before finished.", pipeline.getId());

        pipeline.setStatus(stateContext.getTarget().getId());//결국 마지막 상태를 저장해 줍니다.
        pipeline.setFinishedAt(new Date());// Transaction 내부에 있으므로 자동으로 이 값이 저장이 됩니다.
        pipeline.updateDuration();
    }

    @OnTransitionEndInPipeline()
    public void anyAfterTransition(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Pipeline currentPipeline = (Pipeline)stateContext.getExtendedState().getVariables().get("pipeline");
        if (currentPipeline != null) {
            logger.debug("Updating current pipeline #{} status to '{}'...", currentPipeline.getId(), stateContext.getTarget().getId());
            currentPipeline.setStatus(stateContext.getTarget().getId());
        }

        /*
        after_transition do |pipeline, transition|
        next if transition.loopback?

        pipeline.run_after_commit do
          PipelineHooksWorker.perform_async(id)
          Ci::ExpirePipelineCacheService.new(project, nil)
            .execute(pipeline)
        end
      end
         */
    }

    @OnTransitionStartInPipeline(target = {PipelineStatus.SUCCESS})
    public void afterSuccess(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        //TODO pipeline.run_after_commit { PipelineMetricsWorker.perform_async(id) }
    }

    //TODO Naming
    @OnTransitionStartInPipeline(source = {PipelineStatus.CREATED, PipelineStatus.PENDING, PipelineStatus.RUNNING}, target = {PipelineStatus.SUCCESS})
    public void afterSuccess2(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        //TODO pipeline.run_after_commit { PipelineSuccessWorker.perform_async(id) }
    }

    //TODO naming
    @OnTransitionStartInPipeline(target = {PipelineStatus.SUCCESS, PipelineStatus.FAILED})
    public void afterSuccess3(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        //TODO PipelineNotificationWorker.perform_async(pipeline.id)
    }

    private StateMachine<PipelineStatus, PipelineEvent> getStateMachine(final Pipeline pipeline) {
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachine(
                            new DefaultStateMachineContext<>(
                                    pipeline.getStatus(), null, null, null, null, "pipelineStateMachine"));
                });
        if (!stateMachine.getExtendedState().getVariables().containsKey("pipeline"))
            stateMachine.getExtendedState().getVariables().put("pipeline", pipeline);
        stateMachine.start();

        return stateMachine;
    }

}
