package com.nfl.util.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chi.kim on 6/26/15.
 * PostProcessor is called after the transformation
 * It should have argument of types, (toClass, fromClasses...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PostProcessor {

    String mappingName() default "";

    Class originalClass();
}
