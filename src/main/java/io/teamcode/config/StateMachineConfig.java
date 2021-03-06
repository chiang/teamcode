package io.teamcode.config;

import io.teamcode.common.ci.statemachine.PipelineEvent;
import io.teamcode.domain.entity.ci.Pipeline;
import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.*;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by chiang on 2017. 4. 18..
 */
@Configuration
public class StateMachineConfig {

    @Bean
    public StateMachineEventListener stateMachineLogListener() {
        return new StateMachineEventListener();
    }

    @Configuration
    @EnableStateMachineFactory(name = "pipelineStateMachineFactory")
    public static class PipelineConfig extends StateMachineConfigurerAdapter<PipelineStatus, PipelineEvent> {

        @Autowired
        private StateMachineEventListener stateMachineLogListener;

        @Override
        public void configure(StateMachineConfigurationConfigurer<PipelineStatus, PipelineEvent> config)
                throws Exception {
            config
                    .withConfiguration()
                    .autoStartup(true)
                    .listener(stateMachineLogListener);
        }

        @Override
        public void configure(StateMachineStateConfigurer<PipelineStatus, PipelineEvent> states)
                throws Exception {
            states
                    .withStates()
                    .initial(PipelineStatus.CREATED)
                    //.end(PipelineStatus.)
                    .states(EnumSet.allOf(PipelineStatus.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<PipelineStatus, PipelineEvent> transitions)
                throws Exception {
            transitions
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SKIPPED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.FAILED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.MANUAL).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    //GitLab ????????? ?????? ????????? ??????. ?????????, ???????????? Canceled ??? ?????? ?????? Pending ?????? ???????????? ????????? ????????? ??????...
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CANCELED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    //????????? ????????? ?????? Pending ??? ??? ?????? ????????????. Job ????????? Retry ?????? ????????? ???????????????.
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SUCCESS).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)



                    //Run Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.RUNNING).event(PipelineEvent.RUN)

                    //Drop? FIXME ?????? ??????????????? Failed ??? ?????? ??????????
                    .and()
                    .withExternal()
                    .source(PipelineStatus.RUNNING).target(PipelineStatus.FAILED).event(PipelineEvent.DROP)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.FAILED).event(PipelineEvent.DROP)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.FAILED).event(PipelineEvent.DROP)

                    //Success Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CREATED).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS)
                    .and()
                    .withExternal()
                    .source(PipelineStatus.RUNNING).target(PipelineStatus.SUCCESS).event(PipelineEvent.SUCCESS);

            //Cancel Event
            for (PipelineStatus anyStatus: PipelineStatus.values()) {
                transitions
                        .withExternal()
                        .source(anyStatus).target(PipelineStatus.CANCELED).event(PipelineEvent.CANCEL);
            }
        }


    }

}
