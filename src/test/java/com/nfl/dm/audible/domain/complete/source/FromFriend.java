package com.nfl.dm.audible.domain.complete.source;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class FromFriend {

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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FromFriend)) {
            return false;
        }
        FromFriend ff = (FromFriend) other;
        return ff.getName().equals(this.getName())
                && ff.getAge() == this.getAge()
                && ff.getFavoriteColor().equals(this.getFavoriteColor());
    }
}
