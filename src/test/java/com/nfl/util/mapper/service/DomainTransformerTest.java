package com.nfl.util.mapper.service;

import com.nfl.util.mapper.ApplicationTestConfig;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.Tuple;
import com.nfl.util.mapper.domain.FromObject;
import com.nfl.util.mapper.domain.Student1;
import com.nfl.util.mapper.domain.Student2;
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

        ToObject to = dt.transform(ToObject.class, from, "a");

        Assert.assertEquals(to.getSomeOtherOne(), from.getOne());
    }

    public void testNamedMapping() throws Exception {
        Student1 s1 = new Student1();
        Student2 s2 = dt.transform(Student2.class, s1);
        Assert.assertEquals(s1.getName(), s2.getFirstName() + " " + s2.getLastName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());

        s2 = dt.transformWithMappingName(Student2.class, "reverse", s1);
        Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
    }

    public void testNamedListMapping() throws Exception {
        List<Student1> s1List = new ArrayList<>();
        Student1 s1 = new Student1();
        s1.setName("Joe Montana");
        s1.setAge(25);
        s1.setGpa(2.3);

        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);

        List<Student2> s2List = dt.transformList(Student2.class, s1List, "reverse");

        s2List.parallelStream().forEach(s2 -> {
            Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
            Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
            Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
        });
    }

    public void testDomainTranformerList() throws Exception {
        List<Tuple> fromList = new ArrayList<>();

        Tuple tuple = new Tuple();
        tuple.setObjects(new Object[]{new FromObject(), "a"});
        fromList.add(tuple);
        fromList.add(tuple);
        fromList.add(tuple);
        fromList.add(tuple);
        fromList.add(tuple);


        List<ToObject> toList = dt.transformList(ToObject.class, fromList);

        Assert.assertEquals(toList.get(0).getSomeOtherOne(), new Integer(1));
        Assert.assertEquals(toList.get(0).getSomethingElse(), "10a");
    }

}
