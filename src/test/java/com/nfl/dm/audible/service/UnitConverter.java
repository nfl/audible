package com.nfl.dm.audible.service;

import org.springframework.stereotype.Service;

/**
 * Created by jackson.brodeur on 8/18/15.
 */
@Service
public class UnitConverter {

    public UnitConverter() {

    }

    public double lbsToKgs(double lbs) {
        return lbs * 0.45;
    }

    public double inchesToCentimeters(double inches) {
        return inches * 2.54;
    }
}
