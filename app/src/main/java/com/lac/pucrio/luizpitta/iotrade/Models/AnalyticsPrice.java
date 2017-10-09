package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Modelo onde se armazena as informações de um serviço de analytics
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsPrice implements Serializable {
    private static final long serialVersionUID = 6L;

    /**
     * Variáveis
     */
    private String device;
    private String uuid;
    private String title;
    private String category;
    private double price;
    private double rank;
    private ArrayList<String> services_description;
    private ArrayList<Double> services_prices;

    /**
     * @return Retorna o uuid.
     */
    public String getDevice() {
        return device;
    }

    /**
     * @return Retorna o uuid.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return Retorna o título.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Retorna os comandos de atuação.
     */
    public ArrayList<String> getServicesDescription() {
        return services_description;
    }

    /**
     * @return Retorna o preço dos serviços de analytics.
     */
    public ArrayList<Double> getServicesPrices() {
        return services_prices;
    }

    /**
     * @return Retorna o rank.
     */
    public double getRank() {
        return rank;
    }

    /**
     * @return Retorna o preço.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Método que seta o ranking de qualidade de um serviço
     *
     * @param rank É o ranking de qualidade do serviço {nota/qtd de pessoas que usaram}.
     */
    public void setRank(double rank) {
        this.rank = rank;
    }

    /**
     * Método que seta o preço de um serviço
     *
     * @param price É o preço do serviço.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Método que seta o título de um serviço
     *
     * @param title É o título do serviço.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
