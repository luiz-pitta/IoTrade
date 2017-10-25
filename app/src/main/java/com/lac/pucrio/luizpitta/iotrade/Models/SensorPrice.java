package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model where the information of a sensor is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class SensorPrice implements Serializable {
    private static final long serialVersionUID = 4L;

    /** Attributes */
    private String title;
    private String description;
    private String category;
    private double price;
    private double rank;
    private String category_new;
    private String macAddress;
    private String uuidData;
    private String unit;
    private boolean actuator;
    private ArrayList<String> option_description;
    private ArrayList<String> option_bytes;

    /** Constructor */
    public SensorPrice() {}

    /** Getters */
    public String getUnit() {
        return unit;
    }

    public String getUuidData() {
        return uuidData;
    }

    public String getMacAdress() {
        return macAddress;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getOptionBytes() {
        return option_bytes;
    }

    public ArrayList<String> getOptionDescription() {
        return option_description;
    }

    public boolean isActuator() {
        return actuator;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public double getRank() {
        return rank;
    }
    /** Getters */

    /** Setters */
    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryNew(String category_new) {
        this.category_new = category_new;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }
/** Setters */
}
