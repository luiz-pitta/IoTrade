package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class ConnectPriceWrapper implements Serializable {
    private static final long serialVersionUID = 5L;
    private ConnectPrice connectPrice;

    public ConnectPriceWrapper(ConnectPrice connectPrice) {
        this.connectPrice = connectPrice;
    }

    public ConnectPrice getConnectPrice() {
        return connectPrice;
    }

}