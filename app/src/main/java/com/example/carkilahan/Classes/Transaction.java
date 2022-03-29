package com.example.carkilahan.Classes;

public class Transaction {
    private String id;
    private String amountGiven;
    private String amountToPay;
    private String bookingID;
    private String carID;
    private String dateBooked;
    private String dateWhen;
    private String timeBooked;
    private String timeWhen;
    private String userID;
    private String userName;

    public Transaction(String id, String amountGiven, String amountToPay, String bookingID, String carID, String dateBooked, String dateWhen, String timeBooked, String timeWhen, String userID, String userName) {
        this.id = id;
        this.amountGiven = amountGiven;
        this.amountToPay = amountToPay;
        this.bookingID = bookingID;
        this.carID = carID;
        this.dateBooked = dateBooked;
        this.dateWhen = dateWhen;
        this.timeBooked = timeBooked;
        this.timeWhen = timeWhen;
        this.userID = userID;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmountGiven() {
        return amountGiven;
    }

    public void setAmountGiven(String amountGiven) {
        this.amountGiven = amountGiven;
    }

    public String getAmountToPay() {
        return amountToPay;
    }

    public void setAmountToPay(String amountToPay) {
        this.amountToPay = amountToPay;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getDateBooked() {
        return dateBooked;
    }

    public void setDateBooked(String dateBooked) {
        this.dateBooked = dateBooked;
    }

    public String getDateWhen() {
        return dateWhen;
    }

    public void setDateWhen(String dateWhen) {
        this.dateWhen = dateWhen;
    }

    public String getTimeBooked() {
        return timeBooked;
    }

    public void setTimeBooked(String timeBooked) {
        this.timeBooked = timeBooked;
    }

    public String getTimeWhen() {
        return timeWhen;
    }

    public void setTimeWhen(String timeWhen) {
        this.timeWhen = timeWhen;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
