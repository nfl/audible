package com.nfl.util.mapper.domain.complete.source;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
public class FromJob {

    private String position;

    private int yearsExperience;

    private double annualPay;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public double getAnnualPay() {
        return annualPay;
    }

    public void setAnnualPay(double annualPay) {
        this.annualPay = annualPay;
    }

    @Override
    public String toString() {
        return "[position: " + position + ", yearsExperience: " + yearsExperience + ", annualPay: " + annualPay + "]";
    }
}
