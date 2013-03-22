package com.nfl.util.mapper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Henri Shahrouz
 * @version 4/22/12 8:08 PM
 */
@Retention(RUNTIME)
@Target({ TYPE})
public @interface MappedClass {

}