package com.example.raksheet.majorproject.Process;

import java.util.ArrayList;

/**
 * Created by Raksheet on 21-02-2017.
 */

public class TaskClass {
    String data;
    String code;

    public TaskClass(String data, String code) {
        this.data = data;
        this.code = code;
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
}
