package com.nfl.util.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nfl.util.mapper.MappingType;



@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Mapping {

	MappingType type() default MappingType.FULL;
	
	String name() default "";
	
	Class originalClass() default Object.class;

	boolean parallel() default false;

}
