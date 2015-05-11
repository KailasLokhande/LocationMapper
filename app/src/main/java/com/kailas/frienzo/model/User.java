package com.kailas.frienzo.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String name;

    private String id;

    private String photoUrlGoogle;

    private String photoUrlFacebook;

    private List<String> phoneNumbers;

    private String email;

    private String gPlusProfileUrl;

    private String fbProfileUrl;

    private Location lastKnownLocation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUrlGoogle() {
        return photoUrlGoogle;
    }

    public void setPhotoUrlGoogle(String photoUrlGoogle) {
        this.photoUrlGoogle = photoUrlGoogle;
    }

    public String getPhotoUrlFacebook() {
        return photoUrlFacebook;
    }

    public void setPhotoUrlFacebook(String photoUrlFacebook) {
        this.photoUrlFacebook = photoUrlFacebook;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getgPlusProfileUrl() {
        return gPlusProfileUrl;
    }

    public void setgPlusProfileUrl(String gPlusProfileUrl) {
        this.gPlusProfileUrl = gPlusProfileUrl;
    }

    public String getFbProfileUrl() {
        return fbProfileUrl;
    }

    public void setFbProfileUrl(String fbProfileUrl) {
        this.fbProfileUrl = fbProfileUrl;
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(Location lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj != null && obj instanceof User )
            return this.id.equals(((User)obj).id);
        return false;
    }
}
