package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServiceIoT implements Serializable {
    private static final long serialVersionUID = 4L;

    private String title;
    private String description;
    private Double price;
    private Double lat, lng;

    public ServiceIoT() {
    }

    public ServiceIoT(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

}
