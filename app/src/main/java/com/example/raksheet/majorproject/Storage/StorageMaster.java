package com.example.raksheet.majorproject.Storage;

/**
 * Created by Raksheet on 05-02-2017.
 */

public class StorageMaster {
    int storageID;
    int fragmentID;
    String fileName;
    float filesize;

    public StorageMaster() {
    }

    public StorageMaster(int storageID, int fragmentID, String fileName, float filesize) {
        this.storageID = storageID;
        this.fragmentID = fragmentID;
        this.fileName = fileName;
        this.filesize = filesize;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float getFilesize() {
        return filesize;
    }

    public void setFilesize(float filesize) {
        this.filesize = filesize;
    }
}
