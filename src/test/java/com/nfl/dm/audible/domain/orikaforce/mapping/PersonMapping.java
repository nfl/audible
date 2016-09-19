package com.nfl.dm.audible.domain.orikaforce.mapping;

import com.nfl.dm.audible.CustomMappingWrapper;
import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.domain.orikaforce.target.Person;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@MappingTo(Person.class)
public class PersonMapping {

    @Mapping(originalClass = com.nfl.dm.audible.domain.orikaforce.source.Person.class)
    public Map<String, Function<com.nfl.dm.audible.domain.orikaforce.source.Person, ?>> getMapping() {
        Map<String, Function<com.nfl.dm.audible.domain.orikaforce.source.Person, ?>> map = new HashMap<>();
        map.put("name", (com.nfl.dm.audible.domain.orikaforce.source.Person fa) -> fa.getName());
        map.put("address", (com.nfl.dm.audible.domain.orikaforce.source.Person fa) -> CustomMappingWrapper.customMapping(fa).withOrika(CustomMappingWrapper.Orika.FORCE_ON));

        return map;
    }
}
