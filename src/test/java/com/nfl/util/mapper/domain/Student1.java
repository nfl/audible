package com.nfl.util.mapper.domain;

/**
 * Created by jackson.brodeur on 7/27/15.
 */
public class Student1 {

    private String name = "Tom Brady";

    private Integer age = 18;

    private Double gpa = 3.79;

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
