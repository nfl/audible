package com.nfl.dm.audible;

public final class CustomMappingWrapper {

    private Object object;
    private MappingType mappingType = MappingType.NORMAL;
    private String mappingName = "";
    private Orika orika = Orika.DEFAULT;

    public enum Orika {
        DEFAULT, FORCE_OFF, FORCE_ON
    }

    public Object getObject() {
        return object;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public String getMappingName() {
        return mappingName;
    }

    public Orika getOrika() {
        return orika;
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

    public CustomMappingWrapper withOrika(Orika orika) {
        this.orika = orika;
        return this;
    }
}
