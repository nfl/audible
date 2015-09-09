package com.nfl.dm.audible.domain.complete.target;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class ToFriend {

    private String name;

    private int age;

    private String favoriteColor;

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

    public String getFavoriteColor() {
        return favoriteColor;
    }

    public void setFavoriteColor(String favoriteColor) {
        this.favoriteColor = favoriteColor;
    }

    @Override
    public String toString() {
        return "[name: " + name + ", age: " + age + ", favoriteColor: " + favoriteColor + "]";
    }

}
