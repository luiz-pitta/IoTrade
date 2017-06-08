package com.lac.pucrio.luizpitta.iotrade.Network;

import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

/**
 * Classe responsável pela conexão com o banco através da biblioteca Retrofit
 *
 * @author Luiz Guilherme Pitta
 */
public class NetworkUtil {

    /**
     * @return Retorna uma instância do conector com o servidor.
     */
    public static RetrofitInterface getRetrofit(){

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(rxAdapter)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(RetrofitInterface.class);

    }
}
