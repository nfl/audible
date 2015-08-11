package com.nfl.util.mapper.domain.dummy;

/**
 * Created by jackson.brodeur on 7/27/15.
 */
public class Student2 {

    private String firstName;

    private String lastName;

    private Numbers nums = new Numbers();

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Numbers getNums() {
        return nums;
    }

    public void setNums(Numbers nums) {
        this.nums = nums;
    }

    public String toString() {
        return "[firstName: " + firstName + ", lastName: " + lastName + ", Nums: " + nums.toString() + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Student2)) {
            return false;
        }
        Student2 s = (Student2) other;
        return s.getFirstName().equals(this.getFirstName())
                && s.getLastName().equals(this.getLastName())
                && s.getNums().getAge().equals(this.getNums().getAge())
                && s.getNums().getGpa().equals(this.getNums().getGpa());
    }
}
