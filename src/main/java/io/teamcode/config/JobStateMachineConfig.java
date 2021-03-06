package io.teamcode.config;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Created by chiang on 2017. 4. 18..
 */
@Configuration
public class JobStateMachineConfig {

    @Configuration
    @EnableStateMachineFactory(name = "jobStateMachineFactory")
    public static class JobConfig extends StateMachineConfigurerAdapter<PipelineStatus, PipelineEvent> {

        @Override
        public void configure(StateMachineConfigurationConfigurer<PipelineStatus, PipelineEvent> config)
                throws Exception {
            config
                    .withConfiguration()
                    .machineId("jobStateMachine")
                    .autoStartup(true);
            //.listener(stateMachineLogListener);
        }

        @Override
        public void configure(StateMachineStateConfigurer<PipelineStatus, PipelineEvent> states)
                throws Exception {
            states
                    .withStates()
                    .initial(PipelineStatus.CREATED)
                    .states(EnumSet.allOf(PipelineStatus.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<PipelineStatus, PipelineEvent> transitions)
                throws Exception {
            transitions
                    //Enqueue Event
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SKIPPED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.MANUAL).target(PipelineStatus.PENDING).event(PipelineEvent.PLAY)

                    //Retry Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.FAILED).target(PipelineStatus.PENDING).event(PipelineEvent.RETRY)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CANCELED).target(PipelineStatus.PENDING).event(PipelineEvent.RETRY)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SUCCESS).target(PipelineStatus.PENDING).event(PipelineEvent.RETRY)

                    //Skip Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.SKIPPED).event(PipelineEvent.SKIP)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.SKIPPED).event(PipelineEvent.SKIP)

                    //Cancel Event
                    //GitLab ????????? SKIPPED ??? ?????? Cancel ??? ???????????? ????????? ?????? ??????????????? SKIPPED Transition ???????????? ?????????
                    //Pipeline ??? Cancel ??? ?????? SKIPPED ??? ???????????? ????????? ?????? ?????? ?????????.
                    //CANCELLED ??? ???????????? ?????? ????????? ????????? ?????? (?????? ?????? ??????) ??? ?????????.
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.RUNNING).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.MANUAL).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SKIPPED).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL)

                    //Run Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.RUNNING).event(PipelineEvent.RUN)

                    //Success Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.RUNNING).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS)

                    //Drop Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.FAILED).event(PipelineEvent.DROP)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.FAILED).event(PipelineEvent.DROP)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.RUNNING).target(PipelineStatus.FAILED).event(PipelineEvent.DROP);
        }
    }


}
