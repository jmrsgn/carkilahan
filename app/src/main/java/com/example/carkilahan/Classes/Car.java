package com.example.carkilahan.Classes;

import java.util.List;

public class Car {
    private String carID;
    private String carName;
    private String carPlateNumber;
    private String carColor;
    private String carYear;
    private String carTransmission;
    private String carSeater;
    private String carImgUrl;
    private List<String> carPrices;
    private boolean carIsExpanded;

    public Car(String carID, String carName, String carPlateNumber, String carColor, String carYear, String carTransmission, String carSeater, List<String> carPrices, String carImgUrl) {
        this.carID = carID;
        this.carName = carName;
        this.carPlateNumber = carPlateNumber;
        this.carColor = carColor;
        this.carYear = carYear;
        this.carTransmission = carTransmission;
        this.carSeater = carSeater;
        this.carPrices = carPrices;
        this.carImgUrl = carImgUrl;
        carIsExpanded = false;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarPlateNumber() {
        return carPlateNumber;
    }

    public void setCarPlateNumber(String carPlateNumber) {
        this.carPlateNumber = carPlateNumber;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public String getCarYear() {
        return carYear;
    }

    public void setCarYear(String carYear) {
        this.carYear = carYear;
    }

    public String getCarTransmission() {
        return carTransmission;
    }

    public void setCarTransmission(String carTransmission) {
        this.carTransmission = carTransmission;
    }

    public String getCarSeater() {
        return carSeater;
    }

    public void setCarSeater(String carSeater) {
        this.carSeater = carSeater;
    }

    public String getCarImgUrl() {
        return carImgUrl;
    }

    public void setCarImgUrl(String carImgUrl) {
        this.carImgUrl = carImgUrl;
    }

    public List<String> getCarPrices() {
        return carPrices;
    }

    public void setCarPrices(List<String> carPrices) {
        this.carPrices = carPrices;
    }

    public boolean isCarIsExpanded() {
        return carIsExpanded;
    }

    public void setCarIsExpanded(boolean carIsExpanded) {
        this.carIsExpanded = carIsExpanded;
    }
}