package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.SensorPriceViewHolder;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.ServiceIoTViewHolder;

import java.util.ArrayList;


public class SensorPriceAdapter extends RecyclerArrayAdapter<SensorPrice> {

    private Context context;

    public SensorPriceAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new SensorPriceViewHolder(parent, context);
    }

}
