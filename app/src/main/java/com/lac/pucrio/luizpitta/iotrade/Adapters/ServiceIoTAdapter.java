package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.ServiceIoTViewHolder;

import java.util.ArrayList;
import java.util.List;


public class ServiceIoTAdapter extends RecyclerArrayAdapter<ServiceIoT> {

    private Context context;

    public ServiceIoTAdapter(Context context) {
        super(context);
        this.context = context;
    }

    public void swap(ArrayList<ServiceIoT> list){
        clear();
        addAll(list);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServiceIoTViewHolder(parent, context);
    }

}
