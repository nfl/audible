package com.nfl.dm.audible.domain.dummy;

import com.nfl.dm.audible.MappingType;
import com.nfl.dm.audible.annotation.Mapping;
import com.nfl.dm.audible.annotation.MappingTo;
import com.nfl.dm.audible.annotation.PostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by jackson.brodeur on 7/27/15.
 */
@Component
@MappingTo(Student2.class)
public class Student2Mapping {

    @PostProcessor(originalClass = Student1.class)
    public void postProcess1(Student2 s2, Student1 s1) {
        System.out.println("x");
    }

    @PostProcessor(originalClass = Student1.class)
    public void postProcess2(Student2 s2, Student1 s1) {
        System.out.println("y");
    }

    @Mapping(originalClass = Student1.class)
    public Map<String, Function<Student1, ?>> getMapping() {
        Map<String, Function<Student1, ?>> map = new HashMap<>();
        map.put("firstName", (Student1 s) -> s.getName().split(" ")[0]);
        map.put("lastName", (Student1 s) -> {
            try {
                return s.getName().split(" ")[1];
            } catch (Exception e) {
                return null;
            }
        });
        map.put("nums.age", (Student1 s) -> s.getAge());
        map.put("nums.gpa", (Student1 s) -> s.getGpa());

        return map;
    }

    @Mapping(originalClass = Student1.class, name = "reverse")
    public Map<String, Function<Student1, ?>> reverseMapping() {
        Map<String, Function<Student1, ?>> map = new HashMap<>();
        map.put("lastName", (Student1 s) -> s.getName().split(" ")[0]);
        map.put("firstName", (Student1 s) -> {
            try {
                return s.getName().split(" ")[1];
            } catch (Exception e) {
                return null;
            }
        });
        map.put("nums.age", (Student1 s) -> s.getAge());
        map.put("nums.gpa", (Student1 s) -> s.getGpa());

        return map;
    }

    @Mapping(type = MappingType.EMBEDDED, originalClass = Student1.class, name = "min_add")
    public Map<String, Function<Student1, ?>> minMapping() {
        Map<String, Function<Student1, ?>> map = new HashMap<>();
        map.put("firstName", (Student1 s) -> s.getName().split(" ")[0]);
        map.put("lastName", (Student1 s) -> {
            try {
                return s.getName().split(" ")[1];
            } catch (Exception e) {
                return null;
            }
        });

        map.put("nums.age", (Student1 s) -> s.getAge());
        map.put("nums.gpa", (Student1 s) -> s.getGpa());

        return map;
    }
}

