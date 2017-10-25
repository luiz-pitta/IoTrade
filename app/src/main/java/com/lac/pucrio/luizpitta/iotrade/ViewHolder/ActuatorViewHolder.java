package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.R;

/**
 * ViewHolder where you control the actuator data
 *
 * @author Luiz Guilherme Pitta
 */
public class ActuatorViewHolder extends BaseViewHolder<String> {

    /** Attributes */
    private TextView text1;

    /** Constructor */
    public ActuatorViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_service_iot);

        text1 = $(R.id.text1);
    }

    @Override
    public void setData(String string) {
        text1.setText(string);
    }

}
