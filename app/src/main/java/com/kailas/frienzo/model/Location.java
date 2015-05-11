package com.kailas.frienzo.model;

import java.io.Serializable;

public class Location implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
