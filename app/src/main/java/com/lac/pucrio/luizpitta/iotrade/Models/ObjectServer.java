package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Model where general device information is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class ObjectServer {

    /** Attributes */
    private String service, connectionDevice, analyticsDevice, sensorMacAddress;
    private Double lat, lng;
    private Float radius;

    /** Setters */
    public void setService(String service) {
        this.service = service;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setConnectionDevice(String connectionDevice) {
        this.connectionDevice = connectionDevice;
    }

    public void setAnalyticsDevice(String analyticsDevice) {
        this.analyticsDevice = analyticsDevice;
    }

    public void setSensorMacAddress(String sensorMacAddress) {
        this.sensorMacAddress = sensorMacAddress;
    }
    /** Setters */
}
