package com.nfl.util.mapper.domain;

/**
 * Created by chi.kim on 6/2/15.
 */
public class ToObject {

    private Integer someOtherOne;

    private String somethingElse;

    private Integer auto;

    private Integer anotherAuto;

    private ToNested nested;

    private Nested nested2;

    public Integer getSomeOtherOne() {
        return someOtherOne;
    }

    public void setSomeOtherOne(Integer someOtherOne) {
        this.someOtherOne = someOtherOne;
    }

    public String getSomethingElse() {
        return somethingElse;
    }

    public void setSomethingElse(String somethingElse) {
        this.somethingElse = somethingElse;
    }

    public Integer getAuto() {
        return auto;
    }

    public void setAuto(Integer auto) {
        this.auto = auto;
    }

    public Integer getAnotherAuto() {
        return anotherAuto;
    }

    public void setAnotherAuto(Integer anotherAuto) {
        this.anotherAuto = anotherAuto;
    }

    public ToNested getNested() {
        return nested;
    }

    public void setNested(ToNested nested) {
        this.nested = nested;
    }

    public Nested getNested2() {
        return nested2;
    }

    public void setNested2(Nested nested2) {
        this.nested2 = nested2;
    }
}
