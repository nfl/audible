package com.nfl.util.mapper.service;

import com.nfl.util.mapper.MappingType;

/**
 * Created by chi.kim on 8/19/15.
 */
public class DomainMapperBuilder {


    private MappingType defaultEmbeddedMapping = MappingType.EMBEDDED; //TODO: move to config builder

    private boolean autoMapUsingOrika = true; //TODO: move to config builder

    private boolean parallelProcessEmbeddedList = false; //TODO: move to config builder


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


    public DomainMapper build() {
        return new DomainMapper(this);
    }

    public MappingType getDefaultEmbeddedMapping() {
        return defaultEmbeddedMapping;
    }

    public boolean isAutoMapUsingOrika() {
        return autoMapUsingOrika;
    }

    public boolean isParallelProcessEmbeddedList() {
        return parallelProcessEmbeddedList;
    }
}
