package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.AnalyticsPriceViewHolder;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.SensorPriceViewHolder;

/**
 * Adaptador onde se controla os dados dos sensores
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsAdapter extends RecyclerArrayAdapter<AnalyticsPrice> {

    /**
     * Variáveis
     */
    private Context context;

    /**
     * Classe Builder para construção do Adaptador.
     */
    public AnalyticsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Método do sistema Android, chamado ao criar o adaptador
     */
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new AnalyticsPriceViewHolder(parent);
    }

}
