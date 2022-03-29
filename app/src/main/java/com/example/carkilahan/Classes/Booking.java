package com.example.carkilahan.Classes;

public class Booking {
    private String bookingID;
    private String bookingCarID;
    private String bookingDateWhen;
    private String bookingDateBooked;
    private String bookingTimeWhen;
    private String bookingTimeBooked;
    private String bookingPrice;
    private String bookingType;
    private String bookingUserID;
    private String bookingUserName;

    public Booking(String bookingID, String bookingCarID, String bookingDateWhen, String bookingDateBooked, String bookingTimeWhen, String bookingTimeBooked, String bookingPrice, String bookingType, String bookingUserID, String bookingUserName) {
        this.bookingID = bookingID;
        this.bookingCarID = bookingCarID;
        this.bookingDateWhen = bookingDateWhen;
        this.bookingDateBooked = bookingDateBooked;
        this.bookingTimeWhen = bookingTimeWhen;
        this.bookingTimeBooked = bookingTimeBooked;
        this.bookingPrice = bookingPrice;
        this.bookingType = bookingType;
        this.bookingUserID = bookingUserID;
        this.bookingUserName = bookingUserName;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getBookingCarID() {
        return bookingCarID;
    }

    public void setBookingCarID(String bookingCarID) {
        this.bookingCarID = bookingCarID;
    }

    public String getBookingDateWhen() {
        return bookingDateWhen;
    }

    public void setBookingDateWhen(String bookingDateWhen) {
        this.bookingDateWhen = bookingDateWhen;
    }

    public String getBookingDateBooked() {
        return bookingDateBooked;
    }

    public void setBookingDateBooked(String bookingDateBooked) {
        this.bookingDateBooked = bookingDateBooked;
    }

    public String getBookingTimeWhen() {
        return bookingTimeWhen;
    }

    public void setBookingTimeWhen(String bookingTimeWhen) {
        this.bookingTimeWhen = bookingTimeWhen;
    }

    public String getBookingTimeBooked() {
        return bookingTimeBooked;
    }

    public void setBookingTimeBooked(String bookingTimeBooked) {
        this.bookingTimeBooked = bookingTimeBooked;
    }

    public String getBookingPrice() {
        return bookingPrice;
    }

    public void setBookingPrice(String bookingPrice) {
        this.bookingPrice = bookingPrice;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public String getBookingUserID() {
        return bookingUserID;
    }

    public void setBookingUserID(String bookingUserID) {
        this.bookingUserID = bookingUserID;
    }

    public String getBookingUserName() {
        return bookingUserName;
    }

    public void setBookingUserName(String bookingUserName) {
        this.bookingUserName = bookingUserName;
    }
}