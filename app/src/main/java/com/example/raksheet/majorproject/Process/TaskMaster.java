package com.example.raksheet.majorproject.Process;

/**
 * Created by Raksheet on 21-02-2017.
 */

public class TaskMaster {
    int taskID;
    String code;
    String data;
    int status;
    float complexity;

    public TaskMaster(int taskID, String code, String data, int status, float complexity) {
        this.taskID = taskID;
        this.code = code;
        this.data = data;
        this.status = status;
        this.complexity = complexity;
    }

    public TaskMaster() {

    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getComplexity() {
        return complexity;
    }

    public void setComplexity(float complexity) {
        this.complexity = complexity;
    }
}
