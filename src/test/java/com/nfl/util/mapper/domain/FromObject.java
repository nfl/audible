package com.nfl.util.mapper.domain;

/**
 * Created by chi.kim on 6/2/15.
 */
public class FromObject {

    private Integer one = 1;

    private Nested nested = new Nested();

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
}
