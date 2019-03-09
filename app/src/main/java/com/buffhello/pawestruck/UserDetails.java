package com.buffhello.pawestruck;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

/**
 * POJO for a user's document in Firestore
 */
public class UserDetails {

    private String name, emailId, phoneNum = null, address = null, profilePicUrl = null;
    private ArrayList<String> bookmarkIds = new ArrayList<>();
    private GeoPoint coOrdinates = null;

    UserDetails() {
    }

    UserDetails(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ArrayList<String> getBookmarkIds() {
        return bookmarkIds;
    }

    String getEmailId() {
        return emailId;
    }

    String getPhoneNum() {
        return phoneNum;
    }

    String getAddress() {
        return address;
    }

    String getProfilePicUrl() {
        return profilePicUrl;
    }

    GeoPoint getCoOrdinates() {
        return coOrdinates;
    }
}
