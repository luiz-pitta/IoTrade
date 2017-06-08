package com.lac.pucrio.luizpitta.iotrade.Adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.lac.pucrio.luizpitta.iotrade.ViewHolder.ServiceIoTViewHolder;

import java.util.ArrayList;

/**
 * Adaptador onde se controla os dados de categorias
 *
 * @author Luiz Guilherme Pitta
 */
public class ServiceIoTAdapter extends RecyclerArrayAdapter<String> {

    /**
     * Variáveis
     */
    private Context context;

    /**
     * Classe Builder para construção do Adaptador.
     */
    public ServiceIoTAdapter(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para atualizar parametros dos sensores
     *
     * @param list Lista com os novas categorias para trocar com a lista antiga e atualizar a interface.
     */
    public void swap(ArrayList<String> list){
        clear();
        addAll(list);
    }

    /**
     * Método do sistema Android, chamado ao criar o adaptador
     */
    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ServiceIoTViewHolder(parent);
    }

}
