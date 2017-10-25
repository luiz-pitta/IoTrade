package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;

/**
 * Wrapper for SensorPrice Model
 *
 * @author Luiz Guilherme Pitta
 */
public class SensorPriceWrapper implements Serializable {
    private static final long serialVersionUID = 4L;

    /** Attributes */
    private SensorPrice sensorPrice;

    /** Constructor */
    public SensorPriceWrapper(SensorPrice sensorPrice) {
        this.sensorPrice = sensorPrice;
    }

    /** Getters */
    public SensorPrice getSensorPrice() {
        return sensorPrice;
    }

}