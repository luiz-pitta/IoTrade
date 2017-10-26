package com.lac.pucrio.luizpitta.iotrade.Network;

import android.content.Context;

import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Network base function using REST Api (Retrofit)
 * @author Luiz Guilherme Pitta
 */
public class NetworkUtil {

    public static RetrofitInterface getRetrofit(Context c){

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        String baseUrl = "http://" + AppUtils.getIpAddress(c) + ":" + AppUtils.getServerPort(c) + "/api/v1/";

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitInterface.class);

    }
}
