package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.R;

import rx.subscriptions.CompositeSubscription;


public class SensorPriceViewHolder extends BaseViewHolder<SensorPrice> implements View.OnClickListener {
    private TextView text1, text2, text3, text4;
    private ProgressBar progressIcon;
    private ImageView profilePhoto, moreVerticalIcon;
    private CompositeSubscription mSubscriptions;
    private Context context;

    public SensorPriceViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_sensor_price);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        profilePhoto = $(R.id.profilePhoto);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        progressIcon = $(R.id.progressIcon);
        this.context = context;

        moreVerticalIcon.setOnClickListener(this);

        mSubscriptions = new CompositeSubscription();
    }


    @Override
    public void setData(SensorPrice sensorPrice) {
        text1.setText(sensorPrice.getTitle());
        text2.setText(sensorPrice.getDescription());
        text3.setText(sensorPrice.getCategory());
        text4.setText(String.valueOf(sensorPrice.getPrice()));
    }

    @Override
    public void onClick(View v) {
        if (v == moreVerticalIcon) {
        }
    }

    public void setProgress(boolean progress) {
        if (progress)
            progressIcon.setVisibility(View.VISIBLE);
        else
            progressIcon.setVisibility(View.GONE);

    }
}
