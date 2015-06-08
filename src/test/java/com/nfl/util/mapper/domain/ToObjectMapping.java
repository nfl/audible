package com.nfl.util.mapper.domain;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingClassOf;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by chi.kim on 6/2/15.
 */
@Component
@MappingClassOf(ToObject.class)
public class ToObjectMapping {

    @Mapping(value = MappingType.FULL, originalClasses = {FromObject.class})
    public Map<String, Function<FromObject, ?>> getMapping() {
        Map<String, Function<FromObject, ?>> map = new HashMap<>();
        map.put("someOtherOne", (FromObject o) -> o.getOne());
        map.put("somethingElse", (FromObject o) -> o.getNested().getOne());
        return map;
    }
}
