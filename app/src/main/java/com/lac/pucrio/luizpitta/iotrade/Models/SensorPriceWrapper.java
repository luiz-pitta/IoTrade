package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class SensorPriceWrapper implements Serializable {
    private static final long serialVersionUID = 4L;
    private SensorPrice sensorPrice;

    public SensorPriceWrapper(SensorPrice sensorPrice) {
        this.sensorPrice = sensorPrice;
    }

    public SensorPrice getSensorPrice() {
        return sensorPrice;
    }

}