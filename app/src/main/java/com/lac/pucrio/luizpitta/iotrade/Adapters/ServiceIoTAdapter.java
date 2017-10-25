package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.ServiceIoTViewHolder;

import java.util.ArrayList;

/**
 * Adapter where you control the category data
 *
 * @author Luiz Guilherme Pitta
 */
public class ServiceIoTAdapter extends RecyclerArrayAdapter<String> {

    /**
     * Variables
     */
    private Context context;

    /**
     * Builder Class for Adapter construction.
     */
    public ServiceIoTAdapter(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Method that will make the request to the server to update parameters of the sensors
     *
     * @param list List with the new categories to switch to the old list and refresh the interface.
     */
    public void swap(ArrayList<String> list){
        clear();
        addAll(list);
    }

    /**
     * Android System Method Called When Creating the Adapter
     */
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServiceIoTViewHolder(parent);
    }

}
