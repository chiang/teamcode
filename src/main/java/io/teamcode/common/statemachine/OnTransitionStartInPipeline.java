package io.teamcode.common.statemachine;

import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.statemachine.annotation.OnStateChanged;
import org.springframework.statemachine.annotation.OnTransitionStart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chiang on 2017. 4. 20..
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnTransitionStart
public @interface OnTransitionStartInPipeline {

    PipelineStatus[] source() default {};

    PipelineStatus[] target() default {};
}
