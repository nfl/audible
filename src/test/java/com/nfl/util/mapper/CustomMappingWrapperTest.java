package com.nfl.util.mapper;

import org.testng.Assert;
import org.testng.annotations.Test;
import static com.nfl.util.mapper.CustomMappingWrapper.*;

/**
 * Created by chi.kim on 8/13/15.
 */
@Test
public class CustomMappingWrapperTest {

    public void testCustomMappingWrapper() {
        CustomMappingWrapper cmw = customMapping(new String("a")).withName("mappingName").withType(MappingType.NORMAL);

        Assert.assertEquals(cmw.getMappingName(), "mappingName");
        Assert.assertEquals(cmw.getMappingType(), MappingType.NORMAL);
        Assert.assertEquals(cmw.getObject(), "a");
    }
}
