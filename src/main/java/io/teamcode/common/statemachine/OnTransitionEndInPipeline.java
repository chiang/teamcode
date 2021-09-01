package io.teamcode.common.statemachine;

import io.teamcode.domain.entity.ci.PipelineStatus;
import org.springframework.statemachine.annotation.OnTransitionEnd;
import org.springframework.statemachine.annotation.OnTransitionStart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chiang on 2017. 4. 21..
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnTransitionEnd
public @interface OnTransitionEndInPipeline {

    PipelineStatus[] source() default {};

    PipelineStatus[] target() default {};
}
