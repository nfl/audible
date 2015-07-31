package com.nfl.util.mapper.domain.dummy;

import com.nfl.util.mapper.MappingType;
import com.nfl.util.mapper.annotation.Mapping;
import com.nfl.util.mapper.annotation.MappingTo;
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

    @Mapping(value = MappingType.FULL, originalClass = Student1.class)
    public Map<String, Function<Student1, ?>> getMapping() {
        Map<String, Function<Student1, ?>> map = new HashMap<>();
        map.put("firstName", (Student1 s) -> s.getName().split(" ")[0]);
        try {
            map.put("lastName", (Student1 s) -> s.getName().split(" ")[1]);
        } catch (Exception e) {
            map.put("lastName", (Student1 s) -> null);
        }
        map.put("nums.age", (Student1 s) -> s.getAge());
        map.put("nums.gpa", (Student1 s) -> s.getGpa());

        return map;
    }

    @Mapping(value = MappingType.FULL, originalClass = Student1.class, name = "reverse")
    public Map<String, Function<Student1, ?>> reverseMapping() {
        Map<String, Function<Student1, ?>> map = new HashMap<>();
        map.put("lastName", (Student1 s) -> s.getName().split(" ")[0]);
        try {
            map.put("firstName", (Student1 s) -> s.getName().split(" ")[1]);
        } catch (Exception e) {
            map.put("firstName", (Student1 s) -> null);
        }
        map.put("nums.age", (Student1 s) -> s.getAge());
        map.put("nums.gpa", (Student1 s) -> s.getGpa());

        return map;
    }
}

