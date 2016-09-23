package com.ciriousjoker.homingcompass;

import java.io.Serializable;

public class MyLocationItem implements Serializable {
    private static final long serialVersionUID = -5435670920302756945L;

    private String name = "";
    private boolean isEditing = false;
    private double latitude;
    private double longitude;

    public MyLocationItem(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}