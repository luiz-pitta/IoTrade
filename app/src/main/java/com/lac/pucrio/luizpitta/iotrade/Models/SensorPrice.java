package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Modelo onde se armazena as informações de um sensor
 *
 * @author Luiz Guilherme Pitta
 */
public class SensorPrice implements Serializable {
    private static final long serialVersionUID = 4L;

    /**
     * Variáveis
     */
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

    /**
     * Classe Builder para construção do Modelo.
     */
    public SensorPrice() {}

    /**
     * @return Retorna o unidade de medida.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @return Retorna o título.
     */
    public String getUuidData() {
        return uuidData;
    }

    /**
     * @return Retorna o título.
     */
    public String getMacAdress() {
        return macAddress;
    }

    /**
     * @return Retorna o título.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Retorna a descrição.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Retorna os comandos de atuação.
     */
    public ArrayList<String> getOptionBytes() {
        return option_bytes;
    }

    /**
     * @return Retorna a descrição dos serviços de atuação.
     */
    public ArrayList<String> getOptionDescription() {
        return option_description;
    }

    /**
     * @return Retorna se é atuador.
     */
    public boolean isActuator() {
        return actuator;
    }

    /**
     * @return Retorna a categoria.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return Retorna o preço.
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return Retorna o rank.
     */
    public double getRank() {
        return rank;
    }

    /**
     * Método que seta a categoria do sensor
     *
     * @param category categoria
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Método que seta a categoria nova do sensor
     *
     * @param category_new categoria
     */
    public void setCategoryNew(String category_new) {
        this.category_new = category_new;
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
     * Método que seta o macAddress do sensor
     *
     * @param macAddress macAddress
     */
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Método que seta o preço do sensor
     *
     * @param price preço
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Método que seta o ranking do sensor {nota/qtd de pessoas que usaram}.
     *
     * @param rank ranking
     */
    public void setRank(double rank) {
        this.rank = rank;
    }

}
