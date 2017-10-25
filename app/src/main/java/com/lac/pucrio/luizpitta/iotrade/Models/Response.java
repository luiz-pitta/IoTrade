package com.lac.pucrio.luizpitta.iotrade.Models;

import java.util.ArrayList;

/**
 * Model where information that is returned from the server is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class Response {

    /** Attributes */
    private String message;
    private ArrayList<SensorPrice> sensorPriceArray = new ArrayList<>();
    private ArrayList<ServiceIoT> services = new ArrayList<>();
    private SensorPrice sensor;
    private ConnectPrice connect;
    private AnalyticsPrice analytics;
    private ArrayList<String> categories;
    private User user;
    private double price;

    /** Constructor */
    public Response(SensorPrice sensor, ConnectPrice connect, AnalyticsPrice analytics){
        this.sensor = sensor;
        this.connect = connect;
        this.analytics = analytics;
    }

    /** Getters */
    public ArrayList<SensorPrice> getSensorPrice() {
        return sensorPriceArray;
    }

    public SensorPrice getSensor() {
        return sensor;
    }

    public ConnectPrice getConnect() {
        return connect;
    }

    public double getPrice() {
        return price;
    }

    public AnalyticsPrice getAnalytics() {
        return analytics;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
    /** Getters */
}
