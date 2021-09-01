package io.teamcode.common.security.web;

import io.teamcode.domain.entity.ProjectRole;

import java.lang.annotation.*;

/**
 * 이 Annotation 이 선언되어 있으면 세부적인 권한을 체크합니다.
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProjectSecurity {

    ProjectRole[] value() default {ProjectRole.GUEST};

}
