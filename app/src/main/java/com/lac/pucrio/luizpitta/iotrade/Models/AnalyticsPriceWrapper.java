package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class AnalyticsPriceWrapper implements Serializable {
    private static final long serialVersionUID = 6L;
    private AnalyticsPrice analyticsPrice;

    public AnalyticsPriceWrapper(AnalyticsPrice analyticsPrice) {
        this.analyticsPrice = analyticsPrice;
    }

    public AnalyticsPrice getAnalyticsPrice() {
        return analyticsPrice;
    }

}