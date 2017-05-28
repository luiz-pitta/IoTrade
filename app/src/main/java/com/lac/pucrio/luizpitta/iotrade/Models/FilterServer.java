package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FilterServer implements Serializable {
    private static final long serialVersionUID = 10L;

    private String category;

    private String service;

    private double from, to;
    private Double lat, lng;

    private long id;

    public FilterServer() {
    }

    public void setQuery(String category) {
        this.category = "";
        this.category = this.category.concat("(?i).*");
        this.category = this.category.concat(category);
        this.category = this.category.concat(".*");
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
