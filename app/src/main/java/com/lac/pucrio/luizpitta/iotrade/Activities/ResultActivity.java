package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.infopae.model.SendSensorData;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.base.LocalMessage;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MatchmakingData;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Services.ConnectionService;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Classe Menu da aplicação, onde o usuário seleciona os parêmetros de sua conta na aplicação
 * que irão influenciar no algoritmo de marchmaking.
 *
 * @author Luiz Guilherme Pitta
 */
public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Componentes de interface
     */
    private TextView stopButton, totalText, title, dataText, time;

    /**
     * Variáveis
     */
    private CompositeSubscription mSubscriptions;
    private SensorPriceWrapper sensorPriceWrapper;
    private ConnectPriceWrapper connectPriceWrapper;
    private AnalyticsPriceWrapper analyticsPriceWrapper;
    private double totalPrice = 0.0, timePassed = 0.0, priceTarget;
    private long timeStart = 0;
    private boolean keepRunning = true;
    private Double currentPrice;

    /**
     * Método do sistema Android, chamado ao criar a Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        stopButton = (TextView) findViewById(R.id.stopButton);
        totalText = (TextView) findViewById(R.id.totalText);
        title = (TextView) findViewById(R.id.title);
        dataText = (TextView) findViewById(R.id.dataText);
        time = (TextView) findViewById(R.id.time);

        mSubscriptions = new CompositeSubscription();

        stopButton.setOnClickListener(this);

        String category = getIntent().getStringExtra("category");
        title.setText(category);

        sensorPriceWrapper    = (SensorPriceWrapper) getIntent().getSerializableExtra("sensor_price");
        connectPriceWrapper   = (ConnectPriceWrapper) getIntent().getSerializableExtra("connect_price");
        analyticsPriceWrapper = (AnalyticsPriceWrapper) getIntent().getSerializableExtra("analytics_price");

        EventBus.getDefault().register( this );

        currentPrice = connectPriceWrapper.getConnectPrice().getPrice();

        timeStart = System.currentTimeMillis();

        runThread();
        runThreadTimeElapsed();

        SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
        priceTarget = mSharedPreferences.getFloat("price_target", 20.0f);
    }

    private void runThreadTimeElapsed() {


        new Thread() {
            public void run() {
                while (keepRunning) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String timeElapsed;

                                long millis = ((System.currentTimeMillis() - timeStart));

                                timeElapsed = String.format(Locale.getDefault() ,"%02dm %02ds",
                                        TimeUnit.MILLISECONDS.toMinutes(millis),
                                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                                );;


                                time.setText(timeElapsed);
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void runThread() {


        new Thread() {
            public void run() {
                while (keepRunning) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String formatPrice;

                                double sensor = sensorPriceWrapper.getSensorPrice().getPrice();
                                double analytics = analyticsPriceWrapper.getAnalyticsPrice() == null ? 0.0 : analyticsPriceWrapper.getAnalyticsPrice().getPrice();
                                totalPrice += sensor + currentPrice + analytics;
                                DecimalFormat df = new DecimalFormat("#.00");

                                if(totalPrice < 1)
                                    formatPrice = "0" + df.format(totalPrice);
                                else
                                    formatPrice = df.format(totalPrice);

                                totalText.setText(formatPrice);

                                if(totalPrice >= 0.8*priceTarget && totalPrice < priceTarget)
                                    totalText.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.yellow));
                                else if(totalPrice >= priceTarget)
                                    totalText.setTextColor(Color.RED);

                                ConnectPrice connectPrice = new ConnectPrice();
                                connectPrice.setDevice(connectPriceWrapper.getConnectPrice().getDevice());
                                getConnectPrice(connectPrice);
                            }
                        });
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * Método do sistema Android, chamado ao destruir a Activity
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent( SendSensorData sendSensorData ) {
        if( sendSensorData != null ) {
            Double[] sensorData = sendSensorData.getData();
            String data = dataText.getText().toString() + getString(R.string.data) + "\n";
            data += Arrays.toString(sensorData) + "\n";
            data += getString(R.string.unit) + " " + sensorPriceWrapper.getSensorPrice().getUnit() + "\n";
            data += getString(R.string.date_time) + " " + Utilities.getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm a") + "\n\n";
            dataText.setText(data);

            AppUtils.logger( 'i', Arrays.toString(sendSensorData.getData()), "Connected and Identified..." );
        }
    }

    /**
     * Método do sistema Android, chamado ao ter interação do usuário com algum elemento de interface
     * @see View
     */
    @Override
    public void onClick(View view) {
        if(view == stopButton) {

            if(stopButton.getText().toString().equals(getString(R.string.stop))) {
                keepRunning = false;

                MatchmakingData msg = new MatchmakingData();
                SensorPrice sensorPrice = sensorPriceWrapper.getSensorPrice();
                msg.setUuidClient(AppUtils.getUuid(this).toString());
                msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
                msg.setMacAddress(sensorPrice.getMacAdress());
                msg.setUuidData(sensorPrice.getUuidData());
                msg.setStartStop(MatchmakingData.STOP);

                msg.setRoute(ConnectionService.ROUTE_TAG);
                msg.setPriority(LocalMessage.HIGH);

                EventBus.getDefault().post(msg);

                createDialogRating(sensorPriceWrapper.getSensorPrice(), connectPriceWrapper.getConnectPrice(), analyticsPriceWrapper.getAnalyticsPrice());
            }else if(stopButton.getText().toString().equals(getString(R.string.exit))) {
                finish();
            }
        }
    }

    /**
     * Método do sistema Android, guarda o estado da aplicação para não ser destruido
     * pelo gerenciador de memória do sistema
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Metódo que recebe a resposta do servidor com as informações do usuário atualizado
     *
     *
     * @param response Objeto com o usuário retornado pelo servidor.
     */
    private void handleResponse(Response response) {

        setProgress(false);
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para atualizar parametros dos serviços escolhidos pelo algoritmo
     *
     * @param response Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void updateSensorInformation(Response response) {
        mSubscriptions.add(NetworkUtil.getRetrofit().updateSensorRating(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUpdate,this::handleError));
    }

    /**
     * Metódo que recebe a resposta do servidor se tudo rodou corretamente
     *
     *
     * @param response Retorna mensagem que rodou corretamente.
     */
    private void handleResponseUpdate(Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Metódo que recebe a resposta do servidor caso tenha ocorrido um erro
     *
     *
     * @param error Retorna objeto com o erro que ocorreu.
     */
    private void handleError(Throwable error) {
        setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    /**
     * Se {@code true}, então habilita a barra de progresso
     */
    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }


    /**
     * Metódo cria um diálogo pop-up para perguntar ao usuário se deseja incluir o serviço do analytics
     * ao escolher uma categoria
     *
     * @param sensorPrice Objeto com as informações do Sensor.
     * @param connectPrice Objeto com as informações do serviço de Analytics.
     * @param analyticsPrice Objeto com as informações do serviço de Analytics.
     */
    private void createDialogRating(SensorPrice sensorPrice, ConnectPrice connectPrice, AnalyticsPrice analyticsPrice) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_rating, null);

        SmileRating mSmileRating = (SmileRating) customView.findViewById(R.id.ratingView);
        TextView sensorChosen = (TextView) customView.findViewById(R.id.sensorChosen);

        mSmileRating.setNameForSmile(BaseRating.TERRIBLE, "1");
        mSmileRating.setNameForSmile(BaseRating.BAD, "2");
        mSmileRating.setNameForSmile(BaseRating.OKAY, "3");
        mSmileRating.setNameForSmile(BaseRating.GOOD, "4");
        mSmileRating.setNameForSmile(BaseRating.GREAT, "5");

        DecimalFormat df = new DecimalFormat("#.00");
        String formatPrice;

        if(totalPrice < 1)
            formatPrice = "0" + df.format(totalPrice);
        else
            formatPrice = df.format(totalPrice);

        totalText.setText(formatPrice);

        if(analyticsPrice == null) {
            sensorChosen.setText("Sensor: " + sensorPrice.getTitle() + "\n" +
                    "Nota: " + sensorPrice.getRank() + "\n\n" +
                    "Mobile Hub: " + connectPrice.getTitle() + "\n" +
                    "Nota: " + connectPrice.getRank() + "\n\n" +
                    "Custo total: R$" + formatPrice);
        }
        else {
            sensorChosen.setText("Sensor: " + sensorPrice.getTitle() + "\n" +
                    "Nota: " + sensorPrice.getRank() + "\n\n" +
                    "Mobile Hub: " + connectPrice.getTitle() + "\n" +
                    "Nota: " + connectPrice.getRank() + "\n\n" +
                    "Analytics: " + analyticsPrice.getTitle() + "\n" +
                    "Nota: " + analyticsPrice.getRank() + "\n\n" +
                    "Custo total: R$" + formatPrice);
        }

        mSmileRating.setSelectedSmile(BaseRating.GREAT);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        mSmileRating.setOnRatingSelectedListener(new SmileRating.OnRatingSelectedListener() {
            @Override
            public void onRatingSelected(int level, boolean reselected) {
                Response response;
                sensorPrice.setRank(level);
                connectPrice.setRank(level);
                if(analyticsPrice != null) {
                    analyticsPrice.setRank(level);
                    response = new Response(sensorPrice, connectPrice, analyticsPrice);
                }else
                    response = new Response(sensorPrice, connectPrice, null);
                updateSensorInformation(response);

                stopButton.setText(getString(R.string.exit));

                dialog.dismiss();
            }
        });

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para atualizar o custo do mobile hub
     *
     * @param connectPrice Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void getConnectPrice(ConnectPrice connectPrice) {
        mSubscriptions.add(NetworkUtil.getRetrofit().getConnectPrice(connectPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponsePrice,this::handleError));
    }

    /**
     * Metódo que recebe a resposta do servidor com as informações atualizadas
     *
     *
     * @param response Objeto com o usuário retornado pelo servidor.
     */
    private void handleResponsePrice(Response response) {
        currentPrice = response.getPrice();
        setProgress(false);
    }
}
