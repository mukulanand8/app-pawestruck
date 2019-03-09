package com.buffhello.pawestruck;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

/**
 * POJO for a post in Firestore
 */
class PostDetails {

    /**
     * Checks if card is expanded, post is bookmarked for a user
     */
    private boolean isExpanded, isBookmarked;

    private String postedBy, description, address;
    private String docId = null;
    private Timestamp timestamp;
    private GeoPoint coOrdinates;
    private ArrayList<String> photoUrls;
    private String animal;

    PostDetails() {
    }

    PostDetails(ArrayList<String> photoUrls, String description, String postedBy, String address, Timestamp timestamp, GeoPoint coOrdinates, String animal) {
        this.photoUrls = photoUrls;
        this.postedBy = postedBy;
        this.description = description;
        this.address = address;
        this.timestamp = timestamp;
        this.coOrdinates = coOrdinates;
        this.animal = animal;
    }

    String getAnimal() {
        return animal;
    }

    String getDocId() {
        return docId;
    }

    void setDocId(String docId) {
        this.docId = docId;
    }

    ArrayList<String> getPhotoUrls() {
        return photoUrls;
    }

    String getPostedBy() {
        return postedBy;
    }

    String getDescription() {
        return description;
    }

    boolean isExpanded() {
        return isExpanded;
    }

    void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    boolean isBookmarked() {
        return isBookmarked;
    }

    void setBookmarked(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    Timestamp getTimestamp() {
        return timestamp;
    }

    GeoPoint getCoOrdinates() {
        return coOrdinates;
    }

    String getAddress() {
        return address;
    }
}
