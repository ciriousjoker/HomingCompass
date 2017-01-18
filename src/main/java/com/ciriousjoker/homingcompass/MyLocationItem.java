package com.ciriousjoker.homingcompass;

import java.io.Serializable;

class MyLocationItem implements Serializable {
    private static final long serialVersionUID = -5435670920302756945L;

    private String name = "";
    private boolean isEditing = false;
    private double latitude;
    private double longitude;

    MyLocationItem(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean isEditing() {
        return isEditing;
    }

    void setEditing(boolean editing) {
        isEditing = editing;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        if(!Double.isNaN(latitude))
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}