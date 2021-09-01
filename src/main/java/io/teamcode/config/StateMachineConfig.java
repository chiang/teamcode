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
                    //GitLab 에서는 아래 코드는 없다. 하지만, 실제로는 Canceled 된 것은 다시 Pending 으로 바뀌어야 하므로 이렇게 한다...
                    .and()
                    .withExternal()
                    .source(PipelineStatus.CANCELED).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)
                    //성공한 뒤에도 다시 Pending 이 될 수도 있습니다. Job 하나를 Retry 하는 경우가 해당합니다.
                    .and()
                    .withExternal()
                    .source(PipelineStatus.SUCCESS).target(PipelineStatus.PENDING).event(PipelineEvent.ENQUEUE)



                    //Run Event
                    .and()
                    .withExternal()
                    .source(PipelineStatus.PENDING).target(PipelineStatus.RUNNING).event(PipelineEvent.RUN)

                    //Drop? FIXME 다른 상태에서도 Failed 로 되지 않을까?
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
