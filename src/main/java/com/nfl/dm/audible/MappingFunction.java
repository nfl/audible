package com.nfl.dm.audible;

import java.util.Map;
import java.util.function.Function;


public class MappingFunction {
    boolean forceOrika;
    MappingType mappingType;
    Map<String, Function> mapping;

    public boolean isForceOrika() {
        return forceOrika;
    }

    public void setForceOrika(boolean forceOrika) {
        this.forceOrika = forceOrika;
    }

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

}
