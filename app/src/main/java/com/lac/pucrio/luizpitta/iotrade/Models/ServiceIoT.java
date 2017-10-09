package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Modelo onde se armazena as informações gerais de um serviço genérico
 *
 * @author Luiz Guilherme Pitta
 */
public class ServiceIoT {
    /**
     * Variáveis
     */
    private String title;
    private Double price;
    private Double lat, lng;
    private Float radius;

    /**
     * Classe Builder para construção do Modelo.
     */
    public ServiceIoT() {
    }

    /**
     * @return Retorna o título.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Retorna o preço.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Método que seta o título do sensor
     *
     * @param title título
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Método que seta a latitude baseado no GPS do smartphone
     *
     * @param radius latitude
     */
    public void setRadius(Float radius) {
        this.radius = radius;
    }

    /**
     * Método que seta a latitude baseado no GPS do smartphone
     *
     * @param lat latitude
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * Método que seta a longitude baseado no GPS do smartphone
     *
     * @param lng longitude
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

}
