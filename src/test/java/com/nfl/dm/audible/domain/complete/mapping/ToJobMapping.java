package com.nfl.dm.audible.domain.complete.mapping;

import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.domain.complete.source.FromJob;
import com.nfl.dm.audible.domain.complete.target.ToJob;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/19/15.
 */
@Component
@MappingTo(ToJob.class)
public class ToJobMapping {

    @Mapping(originalClass = FromJob.class)
    public Map<String, Function<FromJob, ?>> getMapping() {
        Map<String, Function<FromJob, ?>> map = new HashMap<>();
        map.put("title", (FromJob fj) -> fj.getPosition());
        map.put("monthlyPay", (FromJob fj) -> fj.getAnnualPay() / 12.0);
        return map;
    }
}
