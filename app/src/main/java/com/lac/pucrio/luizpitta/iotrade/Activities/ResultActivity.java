package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.infopae.model.SendSensorData;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ObjectServer;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.User;
import com.lac.pucrio.luizpitta.iotrade.Models.base.LocalMessage;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MatchmakingData;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Services.ConnectionService;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Class that receives the raw data from a connectivity provider and displays to user
 *
 * @author Luiz Guilherme Pitta
 */
public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Interface Components
     */
    private TextView stopButton, totalText, title, dataText, time;

    /** Attributes */
    private CompositeSubscription mSubscriptions;
    private SensorPriceWrapper sensorPriceWrapper;
    private ConnectPriceWrapper connectPriceWrapper;
    private AnalyticsPriceWrapper analyticsPriceWrapper;
    private double totalPrice = 0.0, timePassed = 0.0, priceTarget;
    private long timeStart = 0, intervalDisconnection = 30;
    private boolean keepRunning = true, keepCalculating = true, lostConnection = false;
    private Double currentPrice;
    private long currentTime, currentTimeAfter, diff, lastTimeData;

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
        lastTimeData = System.currentTimeMillis();

        runThread();
        runThreadTimeElapsed();

        SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
        priceTarget = mSharedPreferences.getFloat("price_target", 20.0f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Thread used to calculate time that has passed and detect connection loss.
     */
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
                                );


                                time.setText(timeElapsed);

                                long lastTimeDiff = (System.currentTimeMillis() - lastTimeData)/1000;
                                if(lastTimeDiff >= (intervalDisconnection* Constants.FACTOR) && !lostConnection) {
                                    lostConnection = true;
                                    setMobileHubDisabled();
                                }
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

    /**
     * Thread used to calculate the price that the user has to pay.
     */
    private void runThread() {


        new Thread() {
            public void run() {
                while (keepRunning) {
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String formatPrice;

                                if(keepCalculating) {
                                    double sensor = sensorPriceWrapper.getSensorPrice().getPrice();
                                    double analytics = analyticsPriceWrapper.getAnalyticsPrice() == null ? 0.0 : analyticsPriceWrapper.getAnalyticsPrice().getPrice();
                                    totalPrice += sensor + currentPrice + analytics;
                                    DecimalFormat df = new DecimalFormat("#.00");

                                    if (totalPrice < 1)
                                        formatPrice = "0" + df.format(totalPrice);
                                    else
                                        formatPrice = df.format(totalPrice);

                                    totalText.setText(formatPrice);

                                    if (totalPrice >= 0.8 * priceTarget && totalPrice < priceTarget)
                                        totalText.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.yellow));
                                    else if (totalPrice >= priceTarget)
                                        totalText.setTextColor(Color.RED);

                                    ConnectPrice connectPrice = new ConnectPrice();
                                    connectPrice.setDevice(connectPriceWrapper.getConnectPrice().getDevice());
                                    getConnectPrice(connectPrice);
                                }
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
     * If {@code true}, enable the progress bar
     */
    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    /**
     * The method used to logout connectivity provider user.
     */
    private void setMobileHubDisabled() {
        User user = new User();
        user.setUuid(UUID.fromString(connectPriceWrapper.getConnectPrice().getUuid()));
        user.setDevice(connectPriceWrapper.getConnectPrice().getDevice());
        user.setActive(false);

        registerLocation(user);
    }

    /**
     * The method used to register state in server of connectivity provider user.
     * @param usr The connectivity provier user.
     */
    private void registerLocation(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit(this).setLocationMobileHub(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseLostConnection,this::handleError));
    }

    /**
     * Method that will redo matchmaking due to connection loss
     *
     */
    private void doMatchmaking(){
        currentTime = Calendar.getInstance().getTimeInMillis();

        SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
        ObjectServer filter = new ObjectServer();

        Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
        Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

        Double lat = Double.parseDouble(mSharedPreferences.getString("latitude_current", "-500.0"));
        Double lng = Double.parseDouble(mSharedPreferences.getString("longitude_current", "-500.0"));

        if(latFixed == -500.0) {
            filter.setLat(lat);
            filter.setLng(lng);
        }else {
            filter.setLat(latFixed);
            filter.setLng(lngFixed);
        }

        filter.setRadius(mSharedPreferences.getFloat("radius", 1.5f));
        filter.setConnectionDevice(connectPriceWrapper.getConnectPrice().getDevice());

        filter.setService(title.getText().toString());

        getSensorChosen(filter);
    }

    /**
     * Method that will make the request to the server to update parameters of the services chosen by the algorithm
     *
     * @param response Object with the parameters to run the algorithm on the server.
     */
    private void updateSensorInformation(Response response) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).updateSensorRating(response)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUpdate,this::handleError));
    }

    /**
     * Method that will make the request to the server to run the matchmaking algorithm without analytics option
     *
     * @param objectServer Object with the parameters to run the algorithm on the server.
     */
    private void getSensorChosen(ObjectServer objectServer) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getSensorAlgorithm(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosen,this::handleError));
    }

    /**
     * Method that will make the request to the server to update the cost of the mobile hub
     *
     * @param connectPrice Object with the parameters to run the algorithm on the server.
     */
    private void getConnectPrice(ConnectPrice connectPrice) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getConnectPrice(connectPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponsePrice,this::handleError));
    }

    /**
     * Method that receives the server response with updated information
     *
     */
    private void handleResponsePrice(Response response) {
        currentPrice = response.getPrice();
        setProgress(false);
    }

    /**
     * Method that receives the response from the server with the set of services chosen
     *
     * @param response Object with the set of services chosen.
     */
    private void handleSensorChosen(Response response) {
        currentTimeAfter = Calendar.getInstance().getTimeInMillis();
        diff = currentTimeAfter-currentTime;
        Log.d("Tempo Matchmaking (Ms)", String.valueOf(diff) + "ms");

        SensorPrice sensorPrice = response.getSensor();

        if( AppUtils.getUuid( this ) == null )
            AppUtils.createSaveUuid( this );

        if(sensorPrice != null) {
            keepCalculating = true;

            MatchmakingData msg = new MatchmakingData();
            msg.setUuidClient(AppUtils.getUuid(this).toString());
            msg.setUuidMatch(response.getConnect().getUuid());
            msg.setMacAddress(response.getSensor().getMacAdress());
            msg.setUuidData(response.getSensor().getUuidData());
            msg.setStartStop(MatchmakingData.START);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            lostConnection = false;

            sensorPriceWrapper = new SensorPriceWrapper(response.getSensor());
            connectPriceWrapper = new ConnectPriceWrapper(response.getConnect());
        }
        else {
            keepRunning = false;
            createDialogRating(sensorPriceWrapper.getSensorPrice(), connectPriceWrapper.getConnectPrice(), null);
            Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     *
     */
    private void handleResponseUpdate(Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     * and starts a new matchmaking
     */
    private void handleResponseLostConnection(Response response) {
        doMatchmaking();
    }

    /**
     * Method that receives the response from the server if an error has occurred.
     *
     * @param error Returns object with the error that occurred.
     */
    private void handleError(Throwable error) {
        setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    /**
     * Method creates a pop-up dialog to ask the user if they want to include the analytics service
     * when choosing a category
     *
     * @param sensorPrice Object with Sensor information.
     * @param connectPrice Object with information from the Analytics service.
     * @param analyticsPrice Object with information from the Analytics service.
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
            String sensorGrade, connectionGrade;

            if (sensorPrice.getRank() < 1)
                sensorGrade = "0" + df.format(sensorPrice.getRank());
            else
                sensorGrade = df.format(sensorPrice.getRank());

            if (connectPrice.getRank() < 1)
                connectionGrade = "0" + df.format(connectPrice.getRank());
            else
                connectionGrade = df.format(connectPrice.getRank());

            sensorChosen.setText("Sensor: " + sensorPrice.getTitle() + "\n" +
                    "Nota: " + sensorGrade + "\n\n" +
                    "Mobile Hub: " + connectPrice.getTitle() + "\n" +
                    "Nota: " + connectionGrade + "\n\n" +
                    "Custo total: R$" + formatPrice);
        }
        else {
            String sensorGrade, connectionGrade, analyticsGrade;

            if (sensorPrice.getRank() < 1)
                sensorGrade = "0" + df.format(sensorPrice.getRank());
            else
                sensorGrade = df.format(sensorPrice.getRank());

            if (connectPrice.getRank() < 1)
                connectionGrade = "0" + df.format(connectPrice.getRank());
            else
                connectionGrade = df.format(connectPrice.getRank());

            if (analyticsPrice.getRank() < 1)
                analyticsGrade = "0" + df.format(analyticsPrice.getRank());
            else
                analyticsGrade = df.format(analyticsPrice.getRank());

            sensorChosen.setText("Sensor: " + sensorPrice.getTitle() + "\n" +
                    "Nota: " + sensorGrade + "\n\n" +
                    "Mobile Hub: " + connectPrice.getTitle() + "\n" +
                    "Nota: " + connectionGrade + "\n\n" +
                    "Analytics: " + analyticsPrice.getDevice() + "\n" +
                    "Nota: " + analyticsGrade + "\n\n" +
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")     // it's actually used to receive events from the Connection Service
    public void onEvent( SendSensorData sendSensorData ) {
        if( sendSensorData != null && sendSensorData.getData() != null) {
            Double[] sensorData = sendSensorData.getData();
            String data = dataText.getText().toString() + getString(R.string.data) + "\n";
            data += Arrays.toString(sensorData) + "\n";
            data += getString(R.string.unit) + " " + sensorPriceWrapper.getSensorPrice().getUnit() + "\n";
            data += getString(R.string.date_time) + " " + Utilities.getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm a") + "\n\n";
            dataText.setText(data);

            lastTimeData = System.currentTimeMillis();
            intervalDisconnection = sendSensorData.getInterval();

            AppUtils.logger( 'i', Arrays.toString(sendSensorData.getData()), "Connected and Identified..." );
        }else if(sendSensorData != null && sendSensorData.getSource() == SendSensorData.MOBILE_HUB){
            keepCalculating = false;

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

            doMatchmaking();
        }
    }
}
