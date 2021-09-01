package io.teamcode.service.ci;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.config.StateMachineConfig;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Created by chiang on 2017. 4. 18..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {StateMachineConfig.class}, loader=AnnotationConfigContextLoader.class)
public class PipelineStateMachineTest {

    @Autowired
    @Qualifier("pipelineStateMachineFactory")
    StateMachineFactory<PipelineStatus, PipelineEvent> stateMachineFactory;

    @Autowired
    @Qualifier("jobStateMachineFactory")
    StateMachineFactory<PipelineStatus, PipelineEvent> jobStateMachineFactory;

    @Test
    public void processStateMachine() {
        StateMachine<PipelineStatus, PipelineEvent> stateMachine1 = stateMachineFactory.getStateMachine("1");
        stateMachine1.start();
        Assert.assertEquals(PipelineStatus.CREATED, stateMachine1.getState().getId());

        stateMachine1.sendEvent(PipelineEvent.ENQUEUE);
        Assert.assertEquals(PipelineStatus.PENDING, stateMachine1.getState().getId());

        stateMachine1.sendEvent(PipelineEvent.RUN);
        Assert.assertEquals(PipelineStatus.RUNNING, stateMachine1.getState().getId());


        StateMachine<PipelineStatus, PipelineEvent> stateMachine2 = stateMachineFactory.getStateMachine("1");
        Assert.assertEquals(PipelineStatus.CREATED, stateMachine2.getState().getId());
    }

    @Test
    public void processJobStateMachine() {
        StateMachine<PipelineStatus, PipelineEvent> stateMachine = jobStateMachineFactory.getStateMachine("jobStateMachine");
        stateMachine.start();
        Assert.assertEquals(PipelineStatus.CREATED, stateMachine.getState().getId());

        stateMachine.sendEvent(PipelineEvent.ENQUEUE);
        Assert.assertEquals(PipelineStatus.PENDING, stateMachine.getState().getId());

        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachine(new DefaultStateMachineContext<>(PipelineStatus.SUCCESS_WITH_WARNINGS, null, null, null, null));
                });
        stateMachine.start();

        Assert.assertEquals(PipelineStatus.SUCCESS_WITH_WARNINGS, stateMachine.getState().getId());
    }
}
