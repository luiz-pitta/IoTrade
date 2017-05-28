package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class SensorPrice implements Serializable {
    private static final long serialVersionUID = 7L;

    private String title;
    private String description;
    private String category;
    private String category_new;
    private double price;
    private double rank;

    public SensorPrice() {
    }

    public SensorPrice(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryNew(String category_new) {
        this.category_new = category_new;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

}
