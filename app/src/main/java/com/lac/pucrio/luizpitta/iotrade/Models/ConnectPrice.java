package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;

/**
 * Model where information from a connectivity provider is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class ConnectPrice implements Serializable {
    private static final long serialVersionUID = 5L;

    /** Attributes */
    private String device;
    private String uuid;
    private String category;
    private double price;
    private double rank;

    /** Getters */
    public String getDevice() {
        return device;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return device;
    }

    public double getRank() {
        return rank;
    }

    public double getPrice() {
        return price;
    }

    /** Setters */
    public void setRank(double rank) {
        this.rank = rank;
    }

    public void setDevice(String device) {
        this.device = device;
    }
    /** Setters */
}
