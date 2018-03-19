package com.example.songkwongwee.myapplication;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Song Kwong Wee on 29/1/2018.
 */

@IgnoreExtraProperties
public class Artist {
    private String userId;    //sets child node as artistID
    private String eventName;  //ditto
    private String eventSport; //ditto

    public Artist(){
        //this constructor is required
    }

    public Artist(String userId, String eventName, String eventSport) {
        this.userId = userId;
        this.eventName = eventName;
        this.eventSport = eventSport;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventSport() {
        return eventSport;
    }
}
