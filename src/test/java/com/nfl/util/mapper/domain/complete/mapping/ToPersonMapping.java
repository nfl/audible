package com.nfl.util.mapper.domain.complete.mapping;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
import com.nfl.util.mapper.domain.complete.source.FromPerson;
import com.nfl.util.mapper.domain.complete.target.ToPerson;
import com.nfl.util.mapper.service.UnitConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
@Component
@MappingTo(ToPerson.class)
public class ToPersonMapping {

    @Autowired
    UnitConverter uc;

    @Mapping(originalClass = FromPerson.class)
    public Map<String, Function<FromPerson, ?>> getMapping() {
        Map<String, Function<FromPerson, ?>> map = new HashMap<>();

        map.put("weightKgs", (FromPerson fp) -> uc.lbsToKgs(fp.getWeightLbs()));
        map.put("heightCentimeters", (FromPerson fp) -> (int) uc.inchesToCentimeters(fp.getHeightInches()));
        map.put("job", (FromPerson fp) -> fp.getJob());

        return map;
    }
}
