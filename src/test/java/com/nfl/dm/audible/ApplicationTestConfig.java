package com.nfl.dm.audible;

import com.nfl.dm.audible.service.DomainMapper;
import com.nfl.dm.audible.service.DomainMapperBuilder;
import com.nfl.dm.audible.service.MappingService;
import com.nfl.dm.audible.service.UnitConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot default configuration for testing.
 */
@Configuration
@ComponentScan(basePackages = {"com.nfl.dm.audible.domain"})
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
