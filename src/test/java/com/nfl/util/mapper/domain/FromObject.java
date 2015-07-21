package com.nfl.util.mapper.domain;

/**
 * Created by chi.kim on 6/2/15.
 */
public class FromObject {

    private Integer one = 1;

    private Nested nested = new Nested();

    private Nested nested2 = new Nested();

    private Integer auto = 5;

    private String anotherAuto = "a";

    public Integer getOne() {
        return one;
    }

    public void setOne(Integer one) {
        this.one = one;
    }

    public Nested getNested() {
        return nested;
    }

    public void setNested(Nested nested) {
        this.nested = nested;
    }

    public Integer getAuto() {
        return auto;
    }

    public void setAuto(Integer auto) {
        this.auto = auto;
    }

    public String getAnotherAuto() {
        return anotherAuto;
    }

    public void setAnotherAuto(String anotherAuto) {
        this.anotherAuto = anotherAuto;
    }

    public Nested getNested2() {
        return nested2;
    }

    public void setNested2(Nested nested2) {
        this.nested2 = nested2;
    }
}
