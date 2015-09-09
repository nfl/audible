package com.nfl.dm.audible.domain.multi_source.source;

/**
 * Created by jackson.brodeur on 8/21/15.
 */
public class FromAnotherAddress {

    private String street;
    
    private String zipPlusFour;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipPlusFour() {
        return zipPlusFour;
    }

    public void setZipPlusFour(String zipPlusFour) {
        this.zipPlusFour = zipPlusFour;
    }
}
