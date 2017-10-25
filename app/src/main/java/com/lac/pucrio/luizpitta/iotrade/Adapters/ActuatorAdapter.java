package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.ActuatorViewHolder;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.SensorPriceViewHolder;

/**
 * Adapter where you control the actuator data
 *
 * @author Luiz Guilherme Pitta
 */
public class ActuatorAdapter extends RecyclerArrayAdapter<String> {

    /**
     * Variables
     */
    private Context context;

    /**
     * Builder Class for Adapter construction.
     */
    public ActuatorAdapter(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Android System Method Called When Creating the Adapter
     */
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActuatorViewHolder(parent);
    }

}
