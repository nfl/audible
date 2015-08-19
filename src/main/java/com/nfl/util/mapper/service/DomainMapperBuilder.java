package com.nfl.util.mapper.service;

import com.nfl.util.mapper.MappingType;

/**
 * Created by chi.kim on 8/19/15.
 */
public class DomainMapperBuilder {


    private MappingType defaultEmbeddedMapping = MappingType.EMBEDDED; //TODO: move to config builder

    private boolean autoMapUsingOrkia = true; //TODO: move to config builder

    private boolean parallelProcessEmbeddedList = false; //TODO: move to config builder


    public DomainMapperBuilder setDefaultEmbeddedMapping(MappingType defaultEmbeddedMapping) {
        this.defaultEmbeddedMapping = defaultEmbeddedMapping;
        return this;
    }

    public DomainMapperBuilder setAutoMapUsingOrkia(boolean autoMapUsingOrkia) {
        this.autoMapUsingOrkia = autoMapUsingOrkia;
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

    public boolean isAutoMapUsingOrkia() {
        return autoMapUsingOrkia;
    }

    public boolean isParallelProcessEmbeddedList() {
        return parallelProcessEmbeddedList;
    }
}
