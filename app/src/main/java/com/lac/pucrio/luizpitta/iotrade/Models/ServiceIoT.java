package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Model where general information of a generic service is stored
 *
 * @author Luiz Guilherme Pitta
 */
public class ServiceIoT {

    /** Attributes */
    private String title;
    private Double price;
    private Double lat, lng;
    private Float radius;
    /** Attributes */

    /** Constructor */
    public ServiceIoT() {
    }

    /** Getters */
    public String getTitle() {
        return title;
    }

    public Double getPrice() {
        return price;
    }
    /** Getters */


    /** Setters */
    public void setTitle(String title) {
        this.title = title;
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
    /** Setters */

}
