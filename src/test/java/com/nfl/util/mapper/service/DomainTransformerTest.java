package com.nfl.util.mapper.service;

import com.nfl.util.mapper.ApplicationTestConfig;
import com.nfl.util.mapper.domain.FromObject;
import com.nfl.util.mapper.domain.ToObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chi.kim on 6/2/15.
 */
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class DomainTransformerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DomainTransformer dt;

    public void testDomainTransformer() throws Exception {
        FromObject from = new FromObject();
        from.setOne(2);

        ToObject to = dt.transform(ToObject.class, from);

        Assert.assertEquals(to.getSomeOtherOne(), from.getOne());
    }

    public void testDomainTranformerList() throws Exception {
        List<FromObject> fromList = new ArrayList<>();
        fromList.add(new FromObject());
        fromList.add(new FromObject());
        fromList.add(new FromObject());
        fromList.add(new FromObject());
        fromList.add(new FromObject());


        List<ToObject> toList = dt.transformList(ToObject.class, fromList);

        Assert.assertEquals(toList.get(0).getSomeOtherOne(), new Integer(1));
        Assert.assertEquals(toList.get(0).getSomethingElse(), new Integer(10));
    }

}
