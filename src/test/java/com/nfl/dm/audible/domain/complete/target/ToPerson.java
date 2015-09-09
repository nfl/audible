package com.nfl.dm.audible.domain.complete.target;

import com.nfl.dm.audible.domain.complete.source.Animal;

import java.util.Set;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class ToPerson {

    private String name;

    private int age;

    private double weightKgs;

    private int heightCentimeters;

    private Set<ToFriend> friends;

    private ToJob job;

    private Set<Animal> pets;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeightKgs() {
        return weightKgs;
    }

    public void setWeightKgs(double weightKgs) {
        this.weightKgs = weightKgs;
    }

    public Set<ToFriend> getFriends() {
        return friends;
    }

    public void setFriends(Set<ToFriend> friends) {
        this.friends = friends;
    }

    public ToJob getJob() {
        return job;
    }

    public void setJob(ToJob job) {
        this.job = job;
    }

    public int getHeightCentimeters() {
        return heightCentimeters;
    }

    public void setHeightCentimeters(int heightCentimeters) {
        this.heightCentimeters = heightCentimeters;
    }

    public Set<Animal> getPets() {
        return pets;
    }

    public void setPets(Set<Animal> pets) {
        this.pets = pets;
    }

    @Override
    public String toString() {
        return "[name: " + name + ", age: " + age + ", weightKgs: " + weightKgs + ", heightCentimeters: " + heightCentimeters +", friends: " + friends
                + ", job: " + job +  ", pets: " + pets + "]";
    }
}
