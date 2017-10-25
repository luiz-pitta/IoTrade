package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

/**
 * Wrapper for ConnectPrice Model
 *
 * @author Luiz Guilherme Pitta
 */
public class ConnectPriceWrapper implements Serializable {
    private static final long serialVersionUID = 5L;

    /** Attributes */
    private ConnectPrice connectPrice;

    /** Constructor */
    public ConnectPriceWrapper(ConnectPrice connectPrice) {
        this.connectPrice = connectPrice;
    }

    /** Getters */
    public ConnectPrice getConnectPrice() {
        return connectPrice;
    }

}