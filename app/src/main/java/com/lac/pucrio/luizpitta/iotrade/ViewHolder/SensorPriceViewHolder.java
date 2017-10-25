package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.R;

/**
 * ViewHolder where you control the sensor data
 *
 * @author Luiz Guilherme Pitta
 */
public class SensorPriceViewHolder extends BaseViewHolder<SensorPrice> {

    /** Attributes */
    private TextView text1, text2, text3, text4;

    /** Constructor */
    public SensorPriceViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_sensor_price);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
    }

    @Override
    public void setData(SensorPrice sensorPrice) {
        text1.setText(sensorPrice.getTitle());
        text2.setText(sensorPrice.getDescription());
        text3.setText(sensorPrice.getCategory());
        text4.setText(String.valueOf(sensorPrice.getPrice()));
    }

}
