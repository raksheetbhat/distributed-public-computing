package com.example.raksheet.majorproject.GCM;

/**
 * Created by Raksheet on 05-02-2017.
 */

import java.sql.Timestamp;

public class Notification {

    String type;    // store or compute
    String subType; // send or receive
    String url;     // Resource to be downloaded
    String fileName;
    int storageID;
    int fragmentID;
    Timestamp expiry;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getSubType() {
        return subType;
    }
    public void setSubType(String subType) {
        this.subType = subType;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public int getStorageID() {
        return storageID;
    }
    public void setStorageID(int storageID) {
        this.storageID = storageID;
    }
    public int getFragmentID() {
        return fragmentID;
    }
    public void setFragmentID(int fragmentID) {
        this.fragmentID = fragmentID;
    }
    public Timestamp getExpiry() {
        return expiry;
    }
    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
