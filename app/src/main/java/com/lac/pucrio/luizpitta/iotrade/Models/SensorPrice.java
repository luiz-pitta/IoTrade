package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Modelo onde se armazena as informações de um sensor
 *
 * @author Luiz Guilherme Pitta
 */
public class SensorPrice {

    /**
     * Variáveis
     */
    private String title;
    private String description;
    private String category;
    private double price;
    private double rank;
    private String category_new;

    /**
     * Classe Builder para construção do Modelo.
     */
    public SensorPrice() {}

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
