package com.nfl.dm.audible.domain.auto.mapping;

import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.domain.auto.source.FromAuto;
import com.nfl.dm.audible.domain.auto.target.ToAuto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/20/15.
 */
@Component
@MappingTo(ToAuto.class)
public class ToAutoMapping {

    @Mapping(originalClass = FromAuto.class)
    public Map<String, Function<FromAuto, ?>> getMapping() {
        Map<String, Function<FromAuto, ?>> map = new HashMap<>();

        return map;
    }
}
