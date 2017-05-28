package com.lac.pucrio.luizpitta.iotrade.Models;

import java.util.ArrayList;

public class Response {

    private String message;
    private ArrayList<SensorPrice> sensorPriceArray = new ArrayList<>();
    private ArrayList<ServiceIoT> services = new ArrayList<>();
    private SensorPrice sensor;
    private User user;

    public ArrayList<SensorPrice> getSensorPrice() {
        return sensorPriceArray;
    }

    public SensorPrice getSensor() {
        return sensor;
    }

    public ArrayList<ServiceIoT> getServices() {
        return services;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

}
