package com.nfl.util.mapper.domain.dummy;

/**
 * Created by jackson.brodeur on 7/27/15.
 */
public class Student1 {

    private String name;

    private Integer age;

    private Double gpa;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
