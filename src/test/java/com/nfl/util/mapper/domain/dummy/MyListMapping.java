package com.nfl.util.mapper.domain.dummy;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/7/15.
 */

@Component
@MappingTo(MyList.class)
public class MyListMapping {

    @Mapping(type = MappingType.FULL, originalClass = HashSet.class)
    public Map<String, Function<HashSet, ?>> getMapping() {
        Map<String, Function<HashSet, ?>> map = new HashMap<>();
        map.put("data", (HashSet s) -> {
            List<String> list = new ArrayList<String>();
            list.addAll(s);
            return list;
        });

        return map;
    }

    @Mapping(type = MappingType.FULL, originalClass = HashSet.class, name = "parallel", parallelProcessCollections = true)
    public Map<String, Function<HashSet, ?>> getMappingParallel() {
        Map<String, Function<HashSet, ?>> map = new HashMap<>();
        map.put("data", (HashSet s) -> {
            List<String> list = new ArrayList<String>();
            list.addAll(s);
            return list;
        });

        return map;
    }

}
