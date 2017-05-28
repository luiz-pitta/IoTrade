package com.lac.pucrio.luizpitta.iotrade.ViewHolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;


public class ServiceIoTViewHolder extends BaseViewHolder<ServiceIoT> implements View.OnClickListener {
    private TextView text1, text2, text3;
    private ProgressBar progressIcon;
    private ImageView profilePhoto, moreVerticalIcon;
    private CompositeSubscription mSubscriptions;
    private Context context;

    public ServiceIoTViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_service_iot);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        profilePhoto = $(R.id.profilePhoto);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        progressIcon = $(R.id.progressIcon);
        this.context = context;

        moreVerticalIcon.setOnClickListener(this);

        mSubscriptions = new CompositeSubscription();
    }


    @Override
    public void setData(ServiceIoT serviceIoT) {
        text1.setText(serviceIoT.getTitle());
        text2.setText(serviceIoT.getDescription());
        text3.setText(context.getResources().getString(R.string.list_services_price,String.valueOf(serviceIoT.getPrice())));
    }

    @Override
    public void onClick(View v) {
        if (v == moreVerticalIcon) {
        }
    }

}
