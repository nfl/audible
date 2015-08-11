package com.nfl.util.mapper;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/5/15.
 */
public class MappingFunction {
    MappingType mappingType;
    Map<String, Function> mapping;
    boolean parallelCollections;

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public Map<String, Function> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, Function> mapping) {
        this.mapping = mapping;
    }

    public boolean isParallelCollections() {
        return parallelCollections;
    }

    public void setParallelCollections(boolean parallelCollections) {
        this.parallelCollections = parallelCollections;
    }
}
