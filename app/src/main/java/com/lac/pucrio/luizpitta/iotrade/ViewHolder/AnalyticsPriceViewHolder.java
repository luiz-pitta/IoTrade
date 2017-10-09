package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.R;

/**
 * ViewHolder onde se controla os dados dos sensores
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsPriceViewHolder extends BaseViewHolder<AnalyticsPrice> {

    /**
     * Componentes de interface
     */
    private TextView text1, text2;

    /**
     * Classe Builder para construção da ViewHolder.
     */
    public AnalyticsPriceViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_analytics_price);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
    }


    /**
     * Método do sistema Android, chamado para setar os dados da linha da RecyclerView
     */
    @Override
    public void setData(AnalyticsPrice analyticsPrice) {
        text1.setText(analyticsPrice.getTitle());
        text2.setText(getContext().getString(R.string.list_services_price, String.valueOf(analyticsPrice.getPrice())));
    }

}
