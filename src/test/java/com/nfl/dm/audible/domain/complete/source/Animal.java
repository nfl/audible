package com.nfl.dm.audible.domain.complete.source;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class Animal {

    private String name;

    private String kind;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "[name: " + name + ", kind: " + kind + ", age: " + age + "]";
    }
}
