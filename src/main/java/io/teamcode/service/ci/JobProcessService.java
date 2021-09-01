package io.teamcode.service.ci;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.common.statemachine.OnTransitionEndInPipeline;
import io.teamcode.common.statemachine.OnTransitionStartInPipeline;
import io.teamcode.domain.entity.ci.Job;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chiang on 2017. 4. 21..
 */
@WithStateMachine(id = "jobStateMachine")
@Service
public class JobProcessService {

    private static final Logger logger = LoggerFactory.getLogger(JobProcessService.class);

    @Autowired
    PipelineProcessService pipelineProcessService;

    @Autowired
    @Qualifier("jobStateMachineFactory")
    StateMachineFactory<PipelineStatus, PipelineEvent> jobStateMachineFactory;

    @Transactional
    public void enqueue(final Job job) {
        logger.debug("Job #{} ({}) 을 작업 큐에 넣습니다...", job.getId(), job.getName());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        logger.debug("현재 Job #{} ({}) 의 상태: {}", job.getId(), job.getName(), stateMachine.getState().getId());

        stateMachine.sendEvent(PipelineEvent.ENQUEUE);
    }

    @Transactional
    public void play(final Job job) {
        logger.debug("Manual Job #{} ({}) 을 실행합니다...", job.getId(), job.getName());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        logger.debug("현재 Job #{} ({}) 의 상태: {}", job.getId(), job.getName(), stateMachine.getState().getId());

        stateMachine.sendEvent(PipelineEvent.PLAY);
    }

    @Transactional
    public void retry(final Job job) {
        logger.debug("Job #{} ({} with status {}) 을 다시 실행합니다...", job.getId(), job.getName(), job.getStatus());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        stateMachine.sendEvent(PipelineEvent.RETRY);
    }

    /**
     * Job 을 실행합니다. 실제로 실행하는 것은 아니고 Runner 요청을 받은 후 실행 상태로 두고 Runner 가 실행을 하는 것입니다.
     *
     * @param job
     */
    @Transactional
    public void run(final Job job) {
        logger.debug("Job #{} ({}) 을 실행 상태로 설정합니다...", job.getId(), job.getName());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        stateMachine.sendEvent(PipelineEvent.RUN);
    }

    @Transactional
    public void update(final Job job, final PipelineStatus jobStatus) {
        logger.debug("Job #{} ({}) 상태를 업데이트합니다. 요청받은 상태: {}", job.getId(), job.getName(), jobStatus);
        if (jobStatus == PipelineStatus.SUCCESS) {
            StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);
            stateMachine.sendEvent(PipelineEvent.SUCCESS);
        }
        else if (jobStatus == PipelineStatus.FAILED) {
            logger.debug("Job #{} ({}) 상태가 실패 상태입니다. Drop 이벤트를 전달합니다...", job.getId(), job.getName());
            StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);
            stateMachine.sendEvent(PipelineEvent.DROP);
        }
        else {
            logger.warn("Job #{} ({}) 상태 업데이트 요청을 받았으나 요청받은 상태 ({}) 는 업데이트 대상이 아닙니다. 요청을 건너뜁니다.", job.getId(), job.getName(), jobStatus);
        }
    }

    @Transactional
    public void skip(final Job job) {
        logger.debug("Job #{} ({}) 을 Skip 처리합니다...", job.getId(), job.getName());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        stateMachine.sendEvent(PipelineEvent.SKIP);
    }

    @Transactional
    public void cancel(final Job job) {
        logger.debug("Job #{} ({}) 을 Cancel 처리합니다...", job.getId(), job.getName());
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = getStateMachine(job);

        if (!stateMachine.getExtendedState().getVariables().containsKey("job"))
            stateMachine.getExtendedState().getVariables().put("job", job);

        stateMachine.sendEvent(PipelineEvent.CANCEL);
    }

    /**
     * Map 을 이용해서 StateMachine 을 관리하면 나중에 Clear 할 조건이 까다롭다. 그리고 서버가 사직될 때 어차피 마지막 상태를 Attach 해야 하므로
     * 그냥 처음부터 이런 방식으로 처리한다.
     *
     * @param job
     * @return
     */
    private StateMachine<PipelineStatus, PipelineEvent> getStateMachine(final Job job) {
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = jobStateMachineFactory.getStateMachine("jobStateMachine");

        logger.debug("Job #{} ({}) 의 마지막 상태를 이용해서 StateMachine 을 생성합니다...", job.getId(), job.getName(), job.getStatus());

        //DefaultStateMachineContext 에 ID 를 설정하지 않으면 @OnTransition Annotation 등이 동작하지 않는다.
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachine(
                            new DefaultStateMachineContext<>(job.getStatus(), null, null, null, null, "jobStateMachine"));
                });

        if (!stateMachine.getExtendedState().getVariables().containsKey("job"))
            stateMachine.getExtendedState().getVariables().put("job", job);

        stateMachine.start();

        return stateMachine;
    }

    @OnTransitionStartInPipeline(target = PipelineStatus.PENDING)
    public void beforePending(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Job currentJob = (Job)stateContext.getExtendedState().getVariables().get("job");

        if (currentJob != null) {
            logger.debug("Job '{}' on before pending.", currentJob.getName());

            currentJob.setStatus(PipelineStatus.PENDING);

            pipelineProcessService.updateStatus(currentJob.getPipeline());
        }
        else {
            logger.debug("Current job not found.");
        }
    }

    @OnTransitionStartInPipeline(source = {PipelineStatus.CREATED, PipelineStatus.MANUAL}, target = {PipelineStatus.PENDING, PipelineStatus.RUNNING})
    public void beforeQueueing(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Job currentJob = (Job)stateContext.getExtendedState().getVariables().get("job");

        if (currentJob != null) {
            logger.debug("Job '{}' on before queueing.", currentJob.getName());

            currentJob.setQueuedAt(new Date());// Transaction 내부에 있으므로 자동으로 이 값이 저장이 됩니다.
        }
        else {
            //TODO warning?
        }
    }

    @OnTransitionStartInPipeline(source = {PipelineStatus.CREATED, PipelineStatus.PENDING}, target = {PipelineStatus.RUNNING})
    public void beforeRunning(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Job currentJob = (Job)stateContext.getExtendedState().getVariables().get("job");

        if (currentJob != null) {
            logger.debug("Job #{} on before running.", currentJob.getName());

            currentJob.setStartedAt(new Date());// Transaction 내부에 있으므로 자동으로 이 값이 저장이 됩니다.
        }
        else {
            //TODO warning?
        }
    }

    @OnTransitionStartInPipeline(target = {PipelineStatus.SUCCESS, PipelineStatus.FAILED, PipelineStatus.CANCELED})
    public void beforeFinished(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Job currentJob = (Job)stateContext.getExtendedState().getVariables().get("job");

        if (currentJob != null) {
            logger.debug("Job #{} ({}) on before finished with event {}, state: {}",
                    currentJob.getId(), currentJob.getName(),
                    stateContext.getEvent(), stateContext.getTarget().getId());

            currentJob.setFinishedAt(new Date());// Transaction 내부에 있으므로 자동으로 이 값이 저장이 됩니다.
        }
        else {
            //TODO warning?
        }
    }

    @OnTransitionEndInPipeline
    public void anyAfterTransition(StateContext<PipelineStatus, PipelineEvent> stateContext) {
        Job currentJob = (Job)stateContext.getExtendedState().getVariables().get("job");

        if (currentJob != null) {
            //어떤 이벤트에서 왔던 간에 해당하는 State 를 Job Status 에 반영합니다.
            currentJob.setStatus(stateContext.getTarget().getId());

            //완료되었거나 현재 Job 이 Manual 이었다면 다음 Job 을 실행합니다.
            //모든 Job 은 Parallel 하기 때문에 현재 Job 이 Manual 이라면 나중에 사용자가 실행할 테니까
            //우리는 다른 Job 을 자동으로 실행해야 합니다.
            boolean completeOrManual = currentJob.isCompleted() || currentJob.isManual();

            if (completeOrManual) {
                logger.debug("Current job #{} ({}) state was completed or manual. Processing the next stage jobs asynchronously...", currentJob.getId(), currentJob.getName());

                pipelineProcessService.processAsync(currentJob.getPipeline().getId());
            }
            else {
                logger.debug("Current job #{} ({}) state was changed normally with state '{}'. Updating the current pipeline asynchronously...",
                        currentJob.getId(), currentJob.getName(), stateContext.getTarget().getId());

                pipelineProcessService.updateStatus(currentJob.getPipeline());
            }
        }
        else {
            //TODO warning?
        }

        /* Gitlab Way
        commit_status.run_after_commit do
        pipeline.try do |pipeline|
          if complete? || manual?
            PipelineProcessWorker.perform_async(pipeline.id)
          else
            PipelineUpdateWorker.perform_async(pipeline.id)
          end
        end
      end

         */
    }
}
