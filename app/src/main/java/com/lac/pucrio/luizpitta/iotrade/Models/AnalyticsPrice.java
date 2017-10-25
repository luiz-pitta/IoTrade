package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model where information from an analytics provider is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsPrice implements Serializable {
    private static final long serialVersionUID = 6L;

    /** Attributes */
    private String device;
    private String uuid;
    private String title;
    private String category;
    private double price;
    private double rank;
    private ArrayList<String> services_description;
    private ArrayList<Double> services_prices;

    /** Getters */
    public String getDevice() {
        return device;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getServicesDescription() {
        return services_description;
    }

    public ArrayList<Double> getServicesPrices() {
        return services_prices;
    }

    public double getRank() {
        return rank;
    }

    public double getPrice() {
        return price;
    }
    /** Getters */

    /** Setters */
    public void setRank(double rank) {
        this.rank = rank;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    /** Setters */
}
