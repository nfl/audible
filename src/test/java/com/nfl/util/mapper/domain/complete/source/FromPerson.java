package com.nfl.util.mapper.domain.complete.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class FromPerson {

    private String name;

    private int age;

    private double weightLbs;

    private List<FromFriend> friends;

    private FromJob job;

    private int heightInches;

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

    public double getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(double weightLbs) {
        this.weightLbs = weightLbs;
    }

    public List<FromFriend> getFriends() {
        return friends;
    }

    public void setFriends(List<FromFriend> friends) {
        this.friends = friends;
    }

    public void addFriend(FromFriend friend) {
        if(friends == null) {
            friends = new ArrayList<>();
        }

        friends.add(friend);
    }

    public FromJob getJob() {
        return job;
    }

    public void setJob(FromJob job) {
        this.job = job;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(int heightInches) {
        this.heightInches = heightInches;
    }

    public Set<Animal> getPets() {
        return pets;
    }

    public void setPets(Set<Animal> pets) {
        this.pets = pets;
    }

    @Override
    public String toString() {

        return "[name: " + name + ", age: " + age + ", weightLbs: " + weightLbs + ", heightInches: " + heightInches + ", friends: " + friends
                + ", job: " + job + ", pets: " + pets + "]";
    }
}
