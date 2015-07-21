package com.nfl.util.mapper;

/**
 * Used for passing mulitple arguments on transformList or returning multiple objects.
 */
public class Tuple {
    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }

    private Object[] objects;
}
