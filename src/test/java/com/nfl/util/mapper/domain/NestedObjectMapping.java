package com.nfl.util.mapper.domain;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import rx.functions.Func1;

/**
 * Created by chi.kim on 6/2/15.
 */
@Component
@MappingTo(ToNested.class)
public class NestedObjectMapping {

//    @Mapping(value = MappingType.FULL, originalClasses = {FromObject.class})
//    public Map<String, Function<FromObject, ?>> getMapping() {
//        Map<String, Function<FromObject, ?>> map = new HashMap<>();
//        map.put("someOtherOne", (FromObject o) -> o.getOne());
//        map.put("somethingElse", (FromObject o) -> o.getNested().getOne());
//        return map;
//    }

    @Mapping(value = MappingType.FULL, originalClasses = {Nested.class})
    public Map<String, Func1<Nested, ?>> getMapping() {
        Map<String, Func1<Nested, ?>> map = new HashMap<>();
        map.put("toone", (Nested o) -> o.getOne());
        return map;
    }
}
