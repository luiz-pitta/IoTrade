package com.lac.pucrio.luizpitta.iotrade.Models;

import java.util.ArrayList;

/**
 * Modelo onde se armazena as informações que retornam do servidor
 *
 * @author Luiz Guilherme Pitta
 */
public class Response {

    /**
     * Variáveis
     */
    private String message;
    private ArrayList<SensorPrice> sensorPriceArray = new ArrayList<>();
    private ArrayList<ServiceIoT> services = new ArrayList<>();
    private SensorPrice sensor;
    private ConnectPrice connect;
    private AnalyticsPrice analytics;
    private ArrayList<String> categories;
    private User user;

    /**
     * Classe Builder para construção do Adaptador.
     */
    public Response(SensorPrice sensor, ConnectPrice connect, AnalyticsPrice analytics){
        this.sensor = sensor;
        this.connect = connect;
        this.analytics = analytics;
    }

    /**
     * @return Retorna um array de objetos da classe #SensorPrice.
     */
    public ArrayList<SensorPrice> getSensorPrice() {
        return sensorPriceArray;
    }

    /**
     * @return Retorna um objeto da da classe #SensorPrice.
     */
    public SensorPrice getSensor() {
        return sensor;
    }

    /**
     * @return Retorna um objeto da da classe #ConnectPrice.
     */
    public ConnectPrice getConnect() {
        return connect;
    }

    /**
     * @return Retorna um objeto da classe #AnalyticsPrice.
     */
    public AnalyticsPrice getAnalytics() {
        return analytics;
    }

    /**
     * @return Retorna a lista de categorias.
     */
    public ArrayList<String> getCategories() {
        return categories;
    }

    /**
     * @return Retorna uma String contendo mensagem de erro/sucesso do servidor.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Retorna um objeto da classe #User.
     */
    public User getUser() {
        return user;
    }

}
