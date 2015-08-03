package com.nfl.util.mapper.service;

import com.nfl.util.mapper.ApplicationTestConfig;
import com.nfl.util.mapper.domain.dummy.Student1;
import com.nfl.util.mapper.domain.dummy.Student2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chi.kim on 6/2/15.
 */
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class DomainMapperTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DomainMapper dt;

    private Student1 s1;

    private List<Student1> s1List;

    @BeforeTest
    public void setUp() {
        s1 = new Student1();
        s1.setName("Tom Brady");
        s1.setAge(21);
        s1.setGpa(3.99);

        s1List = new ArrayList<>();

        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
    }

    @Test
    public void testDomainTransformer() throws Exception {
        Student2 s2 = dt.map(Student2.class, s1);
        Assert.assertEquals(s1.getName(), s2.getFirstName() + " " + s2.getLastName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
    }

    @Test
    public void testNamedMapping() throws Exception {
        Student2 s2 = dt.map(Student2.class, s1, "reverse");
        Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
    }

    @Test
    public void testNamedListMapping() throws Exception {
        List<Student1> s1List = new ArrayList<>();

        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);

        List<Student2> s2List = dt.mapList(Student2.class, s1List, "reverse");

        s2List.stream().forEach(s2 -> {
            Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
            Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
            Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
        });
    }

    @Test
    public void testListMapping() throws Exception {

        List<Student2> s2List = dt.mapList(Student2.class, s1List);

        s2List.parallelStream().forEach(s2 -> {
            Assert.assertEquals(s1.getName(), s2.getFirstName() + " " + s2.getLastName());
            Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
            Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
        });
    }

}
