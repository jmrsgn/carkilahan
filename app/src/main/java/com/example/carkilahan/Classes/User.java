package com.example.carkilahan.Classes;

public class User {
    private String userID;
    private String userName;
    private String userContactNo;
    private String userRole;
    private String userImageURL;

    public User(String userID, String userName, String userContactNo, String userRole, String userImageURL) {
        this.userID = userID;
        this.userName = userName;
        this.userContactNo = userContactNo;
        this.userRole = userRole;
        this.userImageURL = userImageURL;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserContactNo() {
        return userContactNo;
    }

    public void setUserContactNo(String userContactNo) {
        this.userContactNo = userContactNo;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserImageURL() {
        return userImageURL;
    }

    public void setUserImageURL(String userImageURL) {
        this.userImageURL = userImageURL;
    }
}
