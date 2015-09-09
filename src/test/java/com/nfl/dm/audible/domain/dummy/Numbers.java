package com.nfl.dm.audible.domain.dummy;


/**
 * Created by jackson.brodeur on 7/27/15.
 */
public class Numbers {

    private Integer age;

    private Double gpa;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public String toString() {
        return "[age: " + age + ", gpa: " + gpa + "]";
    }
}
