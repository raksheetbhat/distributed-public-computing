package com.example.raksheet.majorproject.Login;

/**
 * Created by Raksheet on 03-04-2017.
 */

public class RegisterMaster {
    String userName;
    String password;

    public RegisterMaster(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
