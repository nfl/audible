package com.nfl.dm.audible.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.nfl.dm.audible.MappingType;



@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Mapping {

	MappingType type() default MappingType.NORMAL;
	
	String name() default "";
	
	Class originalClass();

}
