package com.lac.pucrio.luizpitta.iotrade.Models;

/**
 * Modelo onde se armazena as informações de um serviço de conectividade
 *
 * @author Luiz Guilherme Pitta
 */
public class ConnectPrice {

    /**
     * Variáveis
     */
    private String device;
    private String uuid;
    private String category;
    private double price;
    private double rank;

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
        return device;
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

}
