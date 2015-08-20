package com.nfl.util.mapper.domain.complete.mapping;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import com.nfl.util.mapper.domain.complete.source.FromJob;
import com.nfl.util.mapper.domain.complete.target.ToJob;
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
