package com.nfl.util.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by chi.kim on 6/8/15.
 */
@Retention(RUNTIME)
@Target({ TYPE})
public @interface MappingClassOf {

    Class value();
}
