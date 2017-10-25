package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.view.ViewGroup;
import android.widget.TextView;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.R;

/**
 * ViewHolder where you control category data
 *
 * @author Luiz Guilherme Pitta
 */
public class ServiceIoTViewHolder extends BaseViewHolder<String> {

    /** Attributes */
    private TextView text1;

    /** Constructor */
    public ServiceIoTViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_service_iot);

        text1 = $(R.id.text1);
    }

    @Override
    public void setData(String string) {
        text1.setText(string);
    }

}
