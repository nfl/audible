package com.nfl.dm.audible.service;

import com.nfl.dm.audible.MappingType;

/**
 * Created by chi.kim on 8/19/15.
 */
public class DomainMapperBuilder {


    private MappingType defaultEmbeddedMapping = MappingType.EMBEDDED; //TODO: move to config builder

    private boolean autoMapUsingOrika = true; //TODO: move to config builder

    private boolean parallelProcessEmbeddedList = false; //TODO: move to config builder

    private boolean ignoreNullPointerException = false;

    private boolean failOnException = false;

    public DomainMapperBuilder setDefaultEmbeddedMapping(MappingType defaultEmbeddedMapping) {
        this.defaultEmbeddedMapping = defaultEmbeddedMapping;
        return this;
    }

    public DomainMapperBuilder setAutoMapUsingOrika(boolean autoMapUsingOrika) {
        this.autoMapUsingOrika = autoMapUsingOrika;
        return this;
    }

    public DomainMapperBuilder setParallelProcessEmbeddedList(boolean parallelProcessEmbeddedList) {
        this.parallelProcessEmbeddedList = parallelProcessEmbeddedList;
        return this;
    }

    public DomainMapperBuilder setIgnoreNullPointerException(boolean ignoreNullPointerException) {
        this.ignoreNullPointerException = ignoreNullPointerException;
        return this;
    }

    public DomainMapperBuilder setFailOnException(boolean failOnException) {
        this.failOnException = failOnException;
        return this;
    }


    public DomainMapper build() {
        return new DomainMapper(this);
    }

    public MappingType getDefaultEmbeddedMapping() {
        return defaultEmbeddedMapping;
    }

    public boolean isAutoMapUsingOrika() {
        return autoMapUsingOrika;
    }

    public boolean isIgnoreNullPointerException() {
        return ignoreNullPointerException;
    }

    public boolean isFailOnException() {
        return failOnException;
    }

    public boolean isParallelProcessEmbeddedList() {
        return parallelProcessEmbeddedList;
    }
}
