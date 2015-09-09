package com.nfl.dm.audible.domain.dummy;

import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/7/15.
 */

@Component
@MappingTo(MyList.class)
public class MyListMapping {

    @Mapping(originalClass = HashSet.class)
    public Map<String, Function<HashSet, ?>> getMapping() {
        Map<String, Function<HashSet, ?>> map = new HashMap<>();
        map.put("data", (HashSet s) -> {
            List<String> list = new ArrayList<String>();
            list.addAll(s);
            return list;
        });

        return map;
    }



}
