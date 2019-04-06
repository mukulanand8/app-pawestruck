package com.buffhello.pawestruck;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

import androidx.annotation.Keep;

/**
 * POJO for a post in Firestore
 */
@Keep
public class PostDetails {

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
    private Boolean adopted;

    public PostDetails() {
    }

    public PostDetails(ArrayList<String> photoUrls, String description, String postedBy, String address, Timestamp timestamp, GeoPoint coOrdinates, String animal, Boolean adopted) {
        this.photoUrls = photoUrls;
        this.postedBy = postedBy;
        this.description = description;
        this.address = address;
        this.timestamp = timestamp;
        this.coOrdinates = coOrdinates;
        this.animal = animal;
        this.adopted = adopted;
    }

    public Boolean getAdopted() {
        return adopted;
    }

    public void setAdopted(Boolean adopted) {
        this.adopted = adopted;
    }

    public String getAnimal() {
        return animal;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public ArrayList<String> getPhotoUrls() {
        return photoUrls;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public String getDescription() {
        return description;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public GeoPoint getCoOrdinates() {
        return coOrdinates;
    }

    public String getAddress() {
        return address;
    }
}
