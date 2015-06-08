package com.nfl.util.mapper.domain;

import com.nfl.util.mapper.annotation.MappedClass;
import com.nfl.util.mapper.annotation.MappingClass;

/**
 * Created by chi.kim on 6/2/15.
 */
public class ToObject {

    private Integer someOtherOne;

    private Integer somethingElse;

    public Integer getSomeOtherOne() {
        return someOtherOne;
    }

    public void setSomeOtherOne(Integer someOtherOne) {
        this.someOtherOne = someOtherOne;
    }

    public Integer getSomethingElse() {
        return somethingElse;
    }

    public void setSomethingElse(Integer somethingElse) {
        this.somethingElse = somethingElse;
    }
}
