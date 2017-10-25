package com.lac.pucrio.luizpitta.iotrade.Models;

import java.io.Serializable;

/**
 * Wrapper for AnalyticsPrice Model
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsPriceWrapper implements Serializable {
    private static final long serialVersionUID = 6L;

    /** Attributes */
    private AnalyticsPrice analyticsPrice;

    /** Constructor */
    public AnalyticsPriceWrapper(AnalyticsPrice analyticsPrice) {
        this.analyticsPrice = analyticsPrice;
    }

    /** Getters */
    public AnalyticsPrice getAnalyticsPrice() {
        return analyticsPrice;
    }

}