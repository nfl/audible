package com.nfl.util.mapper.domain;

/**
 * Created by chi.kim on 6/2/15.
 */
public class Nested {

    private Integer one = 10;

    private Integer nestedAuto = 5;

    public Integer getOne() {
        return one;
    }

    public void setOne(Integer one) {
        this.one = one;
    }

    public Integer getNestedAuto() {
        return nestedAuto;
    }

    public void setNestedAuto(Integer nestedAuto) {
        this.nestedAuto = nestedAuto;
    }
}
