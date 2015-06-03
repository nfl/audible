package com.nfl.util.mapper.domain;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by chi.kim on 6/2/15.
 */
@Component
public class ToObjectMapping {

    @Mapping(value = MappingType.FULL, originalClasses = {FromObject.class})
    public Map<String, Function> getMapping() {
        Map<String, Function> map = new HashMap<>();
        map.put("someOtherOne", createFunction("one"));
        map.put("somethingElse", createFunction("nested.one"));
        return map;
    }

    private Function createFunction(String propertyName) {
        return o -> getProperty(o, propertyName);
    }

    private Object getProperty(Object o, String propertyName) {

        try {
            return PropertyUtils.getProperty(o, propertyName);
        } catch (Exception e) {
            return null;
        }
    }
}
