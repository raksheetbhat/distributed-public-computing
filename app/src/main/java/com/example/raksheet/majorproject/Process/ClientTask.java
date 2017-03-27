package com.example.raksheet.majorproject.Process;

import java.util.ArrayList;

/**
 * Created by Raksheet on 21-02-2017.
 */

public class ClientTask {
    int taskID;
    int workID;
    String data;
    String code;
    float complexity;

    public ClientTask(int taskID, int workID, String data, String code, float complexity) {
        this.taskID = taskID;
        this.workID = workID;
        this.data = data;
        this.code = code;
        this.complexity = complexity;
    }

    public int getTaskID() {
        return taskID;
    }
    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }
    public int getWorkID() {
        return workID;
    }
    public void setWorkID(int workID) {
        this.workID = workID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public float getComplexity() {
        return complexity;
    }
    public void setComplexity(float complexity) {
        this.complexity = complexity;
    }
}
