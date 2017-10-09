package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.R;

/**
 * ViewHolder onde se controla os dados dos sensores
 *
 * @author Luiz Guilherme Pitta
 */
public class ActuatorViewHolder extends BaseViewHolder<String> {

    /**
     * Componentes de interface
     */
    private TextView text1;

    /**
     * Classe Builder para construção da ViewHolder.
     */
    public ActuatorViewHolder(ViewGroup parent) {
        super(parent, R.layout.list_item_service_iot);

        text1 = $(R.id.text1);
    }


    /**
     * Método do sistema Android, chamado para setar os dados da linha da RecyclerView
     */
    @Override
    public void setData(String string) {
        text1.setText(string);
    }

}
