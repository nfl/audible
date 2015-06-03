package com.nfl.util.mapper;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot default configuration for testing.
 */
@Configuration
@ComponentScan(basePackages = {"com.nfl.util.mapper"})
public class ApplicationTestConfig {
}
