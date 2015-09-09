package com.nfl.dm.audible.domain.multi_source.mapping;

import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.domain.multi_source.source.FromAddress;
import com.nfl.dm.audible.domain.multi_source.source.FromAnotherAddress;
import com.nfl.dm.audible.domain.multi_source.target.ToAddress;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 8/21/15.
 */
@Component
@MappingTo(ToAddress.class)
public class ToAddressMapping {

    @Mapping(originalClass = FromAddress.class)
    public Map<String, Function<FromAddress, ?>> getMapping() {
        Map<String, Function<FromAddress, ?>> map = new HashMap<>();
        map.put("street", (FromAddress fa) -> fa.getStreet());
        map.put("zip", (FromAddress fa) -> fa.getZip());

        return map;
    }

    @Mapping(originalClass = FromAnotherAddress.class)
    public Map<String, Function<FromAnotherAddress, ?>> getAnotherMapping() {
        Map<String, Function<FromAnotherAddress, ?>> map = new HashMap<>();
        map.put("street", (FromAnotherAddress fa) -> fa.getStreet());
        map.put("zip", (FromAnotherAddress fa) -> fa.getZipPlusFour().substring(0, fa.getZipPlusFour().indexOf('-')));

        return map;
    }
}
