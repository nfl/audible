package com.nfl.util.mapper.service;

import com.nfl.util.mapper.CustomMappingObject;
import com.nfl.util.mapper.MappingType;

public class CustomMappingTypeUtil {
    public static CustomMappingObject addCustomMappingType(Object object, MappingType mappingType) {
        return addCustomMappingType(object, mappingType, null);
    }

    public static CustomMappingObject addCustomMappingType(Object object, MappingType mappingType, String mappingName) {

        CustomMappingObject object1 = new CustomMappingObject();

        object1.setObject(object);
        object1.setMappingType(mappingType);
        object1.setMappingName(mappingName);

        return object1;
    }

}
