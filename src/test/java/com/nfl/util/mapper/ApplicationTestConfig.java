package com.nfl.util.mapper;

import com.nfl.util.mapper.service.DomainMapper;
import com.nfl.util.mapper.service.DomainMapperBuilder;
import com.nfl.util.mapper.service.MappingService;
import com.nfl.util.mapper.service.UnitConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot default configuration for testing.
 */
@Configuration
@ComponentScan(basePackages = {"com.nfl.util.mapper.domain.dummy", "com.nfl.util.mapper.domain.complete"})
public class ApplicationTestConfig {

    @Bean
    public MappingService domainService() {
        return new MappingService();
    }

    @Bean
    public DomainMapper domainMapper() {
        return new DomainMapperBuilder().setAutoMapUsingOrika(true).setDefaultEmbeddedMapping(MappingType.EMBEDDED).setParallelProcessEmbeddedList(true).build();
    }

    @Bean
    public UnitConverter unitConverter() {
        return new UnitConverter();
    }

}
