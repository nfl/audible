package com.nfl.dm.audible.domain.complete.mapping;

import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.domain.complete.source.FromPerson;
import com.nfl.dm.audible.domain.complete.target.ToPerson;
import com.nfl.dm.audible.service.UnitConverter;
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
