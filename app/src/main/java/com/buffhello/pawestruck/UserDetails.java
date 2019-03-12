package com.buffhello.pawestruck;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * POJO for a user's document in Firestore
 */
@Keep
public class UserDetails {

    private String name, emailId, phoneNum = null, address = null, profilePicUrl = null;
    private ArrayList<String> bookmarkIds = new ArrayList<>();
    private GeoPoint coOrdinates = null;

    public UserDetails() {
    }

    public UserDetails(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getBookmarkIds() {
        return bookmarkIds;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getAddress() {
        return address;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public GeoPoint getCoOrdinates() {
        return coOrdinates;
    }
}
