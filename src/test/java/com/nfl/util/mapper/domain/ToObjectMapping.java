package com.nfl.util.mapper.domain;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import org.springframework.stereotype.Component;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chi.kim on 6/2/15.
 */
@Component
@MappingTo(ToObject.class)
public class ToObjectMapping {

//    @Mapping(value = MappingType.FULL, originalClasses = {FromObject.class})
//    public Map<String, Function<FromObject, ?>> getMapping() {
//        Map<String, Function<FromObject, ?>> map = new HashMap<>();
//        map.put("someOtherOne", (FromObject o) -> o.getOne());
//        map.put("somethingElse", (FromObject o) -> o.getNested().getOne());
//        return map;
//    }

//    @Mapping(value = MappingType.FULL_AUTO, originalClasses = {FromObject.class})
//    public Map<String, Func1<FromObject, ?>> getMapping() {
//        Map<String, Func1<FromObject, ?>> map = new HashMap<>();
//        map.put("someOtherOne", (FromObject o) -> o.getOne());
//        map.put("somethingElse", (FromObject o) -> o.getNested().getOne());
//        map.put("nested", (FromObject o)-> o.getNested());
//        return map;
//    }

    @Mapping(value = MappingType.FULL, originalClasses = {FromObject.class, String.class})
    public Map<String, Func2<FromObject, String, ?>> getMapping() {
        Map<String, Func2<FromObject, String,  ?>> map = new HashMap<>();
        map.put("someOtherOne", (FromObject o, String a) -> o.getOne());
        map.put("somethingElse", (FromObject o, String a) -> o.getNested().getOne().toString() + a);
        map.put("nested", (FromObject o, String a)-> o.getNested());
        return map;
    }
}
