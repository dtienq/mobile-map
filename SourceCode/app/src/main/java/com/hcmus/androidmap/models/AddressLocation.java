package com.hcmus.androidmap.models;

public class AddressLocation {
    String name;
    Long latitude;
    Long longitude;
    
    AddressLocation(String name,Long newlat,Long newlong) {
         this.name = name;
         this.latitude = newlat;
         this.longitude= newlong;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }
}
