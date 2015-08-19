package com.nfl.util.mapper;

public final class CustomMappingWrapper {

    private Object object;
    private MappingType mappingType = MappingType.TOP_LEVEL;
    private String mappingName = "";

    public Object getObject() {
        return object;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public String getMappingName() {
        return mappingName;
    }

    public static CustomMappingWrapper customMapping(Object object) {
        CustomMappingWrapper cmo = new CustomMappingWrapper();
        cmo.object = object;

        return cmo;
    }

    public CustomMappingWrapper withType(MappingType mappingType) {
        this.mappingType = mappingType;
        return this;
    }

    public CustomMappingWrapper withName(String mappingName) {
        this.mappingName = mappingName;
        return this;
    }
}
