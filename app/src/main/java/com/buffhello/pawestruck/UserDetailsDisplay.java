package com.buffhello.pawestruck;

import java.util.ArrayList;

/**
 * POJO for a post (card) in Home/Bookmarks page meant just for display. This is not stored in Firestore
 */
public class UserDetailsDisplay {

    /**
     * Profile Picture URL, Phone Number, Username, Email, URL List of photos of the pet
     */
    private String URL;
    private String phoneNum;
    private String name;
    private String email;
    private ArrayList<String> photoUrls;

    UserDetailsDisplay() {
    }

    UserDetailsDisplay(String URL, String phoneNum, String name, String email, ArrayList<String> photoUrls) {
        this.URL = URL;
        this.phoneNum = phoneNum;
        this.name = name;
        this.email = email;
        this.photoUrls = photoUrls;
    }

    ArrayList<String> getPhotourls() {
        return photoUrls;
    }

    String getEmail() {
        return email;
    }

    String getPhotoURL() {
        return URL;
    }

    String getPhonenum() {
        return phoneNum;
    }

    public String getName() {
        return name;
    }
}
