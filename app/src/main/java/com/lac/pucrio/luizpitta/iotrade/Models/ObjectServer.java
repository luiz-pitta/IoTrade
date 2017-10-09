package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Modelo onde se armazena as informações gerais do dispositivo
 *
 * @author Luiz Guilherme Pitta
 */
public class ObjectServer {

    /**
     * Variáveis
     */
    private String service;
    private Double lat, lng;
    private Float radius;

    /**
     * Método que seta o nome do serviço
     *
     * @param service Nome do serviço
     */
    public void setService(String service) {
        this.service = service;
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
