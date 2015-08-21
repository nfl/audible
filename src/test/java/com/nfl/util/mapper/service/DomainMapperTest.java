package com.nfl.util.mapper.service;

import com.nfl.util.mapper.ApplicationTestConfig;
import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.domain.auto.source.FromAuto;
import com.nfl.util.mapper.domain.auto.target.ToAuto;
import com.nfl.util.mapper.domain.complete.source.Animal;
import com.nfl.util.mapper.domain.complete.source.FromFriend;
import com.nfl.util.mapper.domain.complete.source.FromJob;
import com.nfl.util.mapper.domain.complete.source.FromPerson;
import com.nfl.util.mapper.domain.complete.target.ToFriend;
import com.nfl.util.mapper.domain.complete.target.ToPerson;
import com.nfl.util.mapper.domain.dummy.MyList;
import com.nfl.util.mapper.domain.dummy.Numbers;
import com.nfl.util.mapper.domain.dummy.Student1;
import com.nfl.util.mapper.domain.dummy.Student2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chi.kim on 6/2/15.
 */
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class DomainMapperTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DomainMapper dm;

    @Autowired
    private UnitConverter uc;

    private Student1 s1;

    private Student1 student;

    private List<Student1> s1List;

    private HashSet<Student1> set;

    private FromPerson fp;

    @BeforeTest
    public void setUp() {
        s1 = new Student1();
        s1.setId(1);
        s1.setName("Tom Brady");
        s1.setAge(21);
        s1.setGpa(3.99);
        s1.setId(1);

        s1List = new ArrayList<>();

        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);
        s1List.add(s1);

        student = new Student1();
        student.setName("Peyton Manning");
        student.setAge(100);
        student.setGpa(2.10);
        set = new HashSet<>();
        set.add(s1);
        set.add(student);

        fp = new FromPerson();

        FromJob fj = new FromJob();
        fj.setAnnualPay(125000);
        fj.setPosition("Associate Engineer");
        fj.setYearsExperience(5);

        FromFriend friend1 = new FromFriend();
        friend1.setName("Chi");
        friend1.setAge(35);
        friend1.setFavoriteColor("Red");

        Set<Animal> pets = new HashSet<>();
        Animal cat = new Animal();
        Animal dog = new Animal();
        cat.setAge(3);
        cat.setKind("Cat");
        cat.setName("Sammy");
        dog.setAge(5);
        dog.setKind("Dog");
        dog.setName("Dexter");

        pets.add(cat);
        pets.add(dog);

        fp.setAge(21);
        fp.setName("Jackson");
        fp.setWeightLbs(185);
        fp.setHeightInches(73);
        fp.setJob(fj);
        fp.addFriend(friend1);
        fp.setPets(pets);
    }

    @Test
    public void testAutoMapping() {
        FromAuto fa = new FromAuto();
        fa.setA(1);
        fa.setB(2);
        fa.setC(3);
        ToAuto ta = dm.map(ToAuto.class, fa);
        Assert.assertEquals(ta.getA(), fa.getA());
        Assert.assertEquals(ta.getB(), fa.getB());
        Assert.assertEquals(ta.getC(), fa.getC());
    }

    @Test
    public void testEmbedded() {
        ToPerson tp = dm.map(ToPerson.class, fp);
        Assert.assertEquals(tp.getJob().getYearsExperience(), fp.getJob().getYearsExperience());
        Assert.assertEquals(tp.getJob().getTitle(), fp.getJob().getPosition());
    }

    @Test
    public void testMapWithExternalService() {
        ToPerson tp = dm.map(ToPerson.class, fp);
        Assert.assertEquals(tp.getWeightKgs(), uc.lbsToKgs(fp.getWeightLbs()));
        Assert.assertEquals(tp.getHeightCentimeters(), (int) uc.inchesToCentimeters(fp.getHeightInches()));

    }

    @Test
    public void testCollectionMapping() {
        ToPerson tp = dm.map(ToPerson.class, fp);
        for(ToFriend tf: tp.getFriends()) {
            FromFriend temp = new FromFriend();
            temp.setName(tf.getName());
            temp.setAge(tf.getAge());
            temp.setFavoriteColor(tf.getFavoriteColor());
            Assert.assertTrue(fp.getFriends().contains(temp));
        }
    }

    @Test
    public void testDomainMapper() throws Exception {
        Student2 s2 = dm.map(Student2.class, s1);
        Assert.assertEquals(s1.getName(), s2.getFirstName() + " " + s2.getLastName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
    }

    @Test
    public void testNamedMapping() throws Exception {
        Student2 s2 = dm.map(Student2.class, s1, "reverse");
        Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
        Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
        Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
    }

    @Test(enabled = false)
    public void timeTest() throws Exception {
        for (int j = 0; j < 20; j++) {
            Instant start = Instant.now();
            for (int i = 0; i < 1000000; i++) {
                dm.map(Student2.class, s1);
            }
            Instant stop = Instant.now();

            System.out.println(Duration.between(start, stop).toString().replaceAll("[^\\d.]", ""));
        }
    }

    @Test
    public void testNamedListMapping() throws Exception {

        List<Student2> s2List = dm.mapList(Student2.class, s1List, "reverse");

        s2List.stream().forEach(s2 -> {
            Assert.assertEquals(s1.getName(), s2.getLastName() + " " + s2.getFirstName());
            Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
            Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
        });
    }

    @Test
    public void testListMapping() throws Exception {

        List<Student2> s2List = dm.mapList(Student2.class, s1List);

        s2List.parallelStream().forEach(s2 -> {
            Assert.assertEquals(s1.getName(), s2.getFirstName() + " " + s2.getLastName());
            Assert.assertEquals(s1.getAge(), s2.getNums().getAge());
            Assert.assertEquals(s1.getGpa(), s2.getNums().getGpa());
        });
    }

    @Test
    public void testInnerCollections() throws Exception {
        MyList list = dm.map(MyList.class, set);
        set.stream().forEach(s -> {
            Student2 temp = new Student2();
            temp.setFirstName(s.getName().split(" ")[0]);
            temp.setLastName(s.getName().split(" ")[1]);
            Numbers n = new Numbers();
            n.setAge(s.getAge());
            n.setGpa(s.getGpa());
            temp.setNums(n);
            Assert.assertTrue(list.getData().contains(temp));
        });
    }

}
