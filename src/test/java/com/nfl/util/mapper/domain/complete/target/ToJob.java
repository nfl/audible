package com.nfl.util.mapper.domain.complete.target;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class ToJob {

    private String title;

    private int yearsExperience;

    private double monthlyPay;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public double getMonthlyPay() {
        return monthlyPay;
    }

    public void setMonthlyPay(double monthlyPay) {
        this.monthlyPay = monthlyPay;
    }

    @Override
    public String toString() {
        return "[title: " + title + ", yearsExperience: " + yearsExperience + ", monthlyPay: " + monthlyPay + "]";
    }
}
