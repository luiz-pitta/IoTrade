package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.UUID;

/**
 * Model where user's are stored
 *
 * @author Luiz Guilherme Pitta
 */
public class User implements Serializable {
    private static final long serialVersionUID = 2L;

    /** Attributes */
    private String name, device;
    private double batery;
    private int signal;
    private double lat, lng;
    private float accuracy;
    private UUID uuid;
    private boolean active = false;
    private double velocity = -1;
    private double budget;
    /** Attributes */

    /** Getters */
    public double getBudget() {
        return budget;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getBatery() {
        return batery;
    }

    public int getSignal() {
        return signal;
    }

    public boolean isActive() {
        return active;
    }
    /** Getters */

    /** Setters */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setBatery(double batery) {
        this.batery = batery;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public float setAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    /** Setters */
}
