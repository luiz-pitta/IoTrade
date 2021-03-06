package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.infopae.model.BuyAnalyticsData;
import com.infopae.model.SendActuatorData;
import com.infopae.model.SendSensorData;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.lac.pucrio.luizpitta.iotrade.Adapters.ActuatorAdapter;
import com.lac.pucrio.luizpitta.iotrade.Adapters.AnalyticsAdapter;
import com.lac.pucrio.luizpitta.iotrade.MainActivity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Class that receives the analyzed data from a analytics provider and displays to user
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Interface Components
     */
    private TextView stopButton, totalText, title, time;
    private EasyRecyclerView recyclerView;

    /** Attributes */
    private CompositeSubscription mSubscriptions;
    private SensorPriceWrapper sensorPriceWrapper;
    private ConnectPriceWrapper connectPriceWrapper;
    private AnalyticsPriceWrapper analyticsPriceWrapper;
    private double totalPrice = 0.0, timePassed = 0.0, priceTarget;
    private long timeStart = 0, intervalDisconnection = 30;
    private boolean keepRunning = true, keepCalculating = true, lostConnection = false;
    private boolean ackConnection = false, ackAnalytics = false, clicked = false;
    private boolean connectionDisabled = false, analyticsDisabled = false;
    private Double currentPrice;
    private AnalyticsAdapter adapter;
    private ArrayList<AnalyticsPrice> analyticsPrices = new ArrayList<>();
    private long currentTime, currentTimeAfter, diff, lastTimeData;
    private Double value_analytics = null;
    private int position_analytics = -1;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        stopButton = (TextView) findViewById(R.id.stopButton);
        totalText = (TextView) findViewById(R.id.totalText);
        title = (TextView) findViewById(R.id.title);
        time = (TextView) findViewById(R.id.time);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this, R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnalyticsAdapter(this);

        recyclerView.setEmptyView(R.layout.empty_list);

        mSubscriptions = new CompositeSubscription();

        stopButton.setOnClickListener(this);

        EventBus.getDefault().register( this );

        String category = getIntent().getStringExtra("category");
        title.setText(category);

        sensorPriceWrapper    = (SensorPriceWrapper) getIntent().getSerializableExtra("sensor_price");
        connectPriceWrapper   = (ConnectPriceWrapper) getIntent().getSerializableExtra("connect_price");
        analyticsPriceWrapper = (AnalyticsPriceWrapper) getIntent().getSerializableExtra("analytics_price");

        int count = analyticsPriceWrapper.getAnalyticsPrice().getServicesDescription().size();
        ArrayList<String> services_description = analyticsPriceWrapper.getAnalyticsPrice().getServicesDescription();
        ArrayList<Double> services_prices = analyticsPriceWrapper.getAnalyticsPrice().getServicesPrices();

        for(int i=0;i<count;i++) {
            String title = services_description.get(i);
            Double price = services_prices.get(i);
            AnalyticsPrice analyticsPrice = new AnalyticsPrice();
            analyticsPrice.setPrice(price);
            analyticsPrice.setTitle(title);
            analyticsPrices.add(analyticsPrice);
        }

        adapter.addAll(analyticsPrices);
        recyclerView.setAdapterWithProgress(adapter);

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {


                if(position == 1)
                    createDialogAlert();
                else {
                    Intent intent;
                    BuyAnalyticsData buyAnalyticsData = new BuyAnalyticsData();
                    buyAnalyticsData.setMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());
                    buyAnalyticsData.setUuidData(sensorPriceWrapper.getSensorPrice().getUuidData());
                    buyAnalyticsData.setOption(position);
                    buyAnalyticsData.setUuidIotrade(AppUtils.getUuid(AnalyticsActivity.this).toString());
                    buyAnalyticsData.setUuidAnalyticsHub(analyticsPriceWrapper.getAnalyticsPrice().getUuid());

                    EventBus.getDefault().post(buyAnalyticsData);

                    totalPrice += adapter.getItem(position).getPrice();
                    position_analytics = position;

                    if (position > 1)
                        intent = new Intent(AnalyticsActivity.this, AnalyticsOptionActivity.class);
                    else
                        intent = new Intent(AnalyticsActivity.this, AnalyticsChartActivity.class);

                    intent.putExtra("title_analytics", adapter.getItem(position).getTitle());

                    clicked = true;

                    startActivityForResult(intent, 133);
                }
            }
        });

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
                stopAnalyticsService();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 133) {
            if(resultCode == RESULT_OK){
                stopAnalyticsService();
                createDialogRating(sensorPriceWrapper.getSensorPrice(), connectPriceWrapper.getConnectPrice(), analyticsPriceWrapper.getAnalyticsPrice());
            }
        }
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

                                //Detect internet loss in providers
                                long lastTimeDiff = (System.currentTimeMillis() - lastTimeData)/1000;
                                if(lastTimeDiff >= (intervalDisconnection* Constants.FACTOR) && !lostConnection && clicked) {
                                    lostConnection = true;

                                    MatchmakingData msg = new MatchmakingData();
                                    SensorPrice sensorPrice = sensorPriceWrapper.getSensorPrice();
                                    msg.setUuidClient(analyticsPriceWrapper.getAnalyticsPrice().getUuid());
                                    msg.setUuidAnalyticsClient(AppUtils.getUuid(AnalyticsActivity.this).toString());
                                    msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
                                    msg.setMacAddress(sensorPrice.getMacAdress());
                                    msg.setUuidData(sensorPrice.getUuidData());
                                    msg.setAck(true);
                                    msg.setStartStop(MatchmakingData.STOP);

                                    msg.setRoute(ConnectionService.ROUTE_TAG);
                                    msg.setPriority(LocalMessage.HIGH);

                                    EventBus.getDefault().post(msg);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!ackAnalytics)
                                                setAnalyticsHubDisabled();

                                            if(!ackConnection)
                                                setMobileHubDisabled();
                                        }
                                    }, Constants.ACK_TIMEOUT);
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
                                        totalText.setTextColor(ContextCompat.getColor(AnalyticsActivity.this, R.color.yellow));
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
     * The method used to logout analytics user.
     */
    private void setAnalyticsHubDisabled() {
        User user = new User();
        user.setUuid(UUID.fromString(analyticsPriceWrapper.getAnalyticsPrice().getUuid()));
        user.setDevice(analyticsPriceWrapper.getAnalyticsPrice().getDevice());
        user.setActive(false);

        registerAnalytics(user);
    }

    /**
     * The method used to register state in server of analytics user.
     * @param usr The new location object.
     */
    private void registerAnalytics(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit(this).setAnalyticsMobileHub(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseLostConnection,this::handleError));
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
     * The method used to stop Analytics Service.
     */
    private void stopAnalyticsService(){
        keepRunning = false;

        MatchmakingData msg = new MatchmakingData();
        SensorPrice sensorPrice = sensorPriceWrapper.getSensorPrice();
        msg.setUuidClient(analyticsPriceWrapper.getAnalyticsPrice().getUuid());
        msg.setUuidAnalyticsClient(AppUtils.getUuid(this).toString());
        msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
        msg.setMacAddress(sensorPrice.getMacAdress());
        msg.setUuidData(sensorPrice.getUuidData());
        msg.setStartStop(MatchmakingData.STOP);

        msg.setRoute(ConnectionService.ROUTE_TAG);
        msg.setPriority(LocalMessage.HIGH);

        EventBus.getDefault().post(msg);

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(AnalyticsActivity.this, getString(R.string.analytics_stop_try_again), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * The method used to close the activity and finish the Analytics Service.
     */
    private void finishAnalyticsNoMatch() {
        Intent intent = new Intent("finish_no_match");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        createDialogRating(sensorPriceWrapper.getSensorPrice(), connectPriceWrapper.getConnectPrice(), analyticsPriceWrapper.getAnalyticsPrice());
        Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();
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
     * Method that will make the request to the server to update the cost of the mobile hub
     *
     * @param connectPrice Object with the parameters to run the algorithm on the server.
     */
    private void getConnectPrice(ConnectPrice connectPrice) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getConnectPrice(connectPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Method that will make the request to the server to run the matchmaking algorithm with analytics option
     *
     * @param objectServer Object with the parameters to run the algorithm on the server.
     */
    private void getSensorChosenAnalytics(ObjectServer objectServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit(this).getSensorAlgorithmAnalytics(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosenAnalytics,this::handleError));
    }

    /**
     * Method that will make the request to the server to run the matchmaking algorithm to choose a analytics provider
     *
     * @param objectServer Object with the parameters to run the algorithm on the server.
     */
    private void getChosenAnalytics(ObjectServer objectServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit(this).getNewAnalytics(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleChosenAnalytics,this::handleError));
    }

    /**
     * Method that receives the response from the server with the set of services chosen
     *
     * @param response Object with the set of services chosen.
     */
    private void handleChosenAnalytics(Response response) {
        AnalyticsPrice analyticsPrice = response.getAnalytics();
        if(analyticsPrice != null){
            MatchmakingData msg = new MatchmakingData();
            msg.setUuidClient(analyticsPrice.getUuid());
            msg.setUuidAnalyticsClient( AppUtils.getUuid(this).toString());
            msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
            msg.setMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());
            msg.setUuidData(sensorPriceWrapper.getSensorPrice().getUuidData());
            msg.setStartStop(MatchmakingData.MODIFY);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            lostConnection = false;

            analyticsPriceWrapper = new AnalyticsPriceWrapper(analyticsPrice);

            BuyAnalyticsData buyAnalyticsData = new BuyAnalyticsData();
            buyAnalyticsData.setMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());
            buyAnalyticsData.setUuidData(sensorPriceWrapper.getSensorPrice().getUuidData());
            buyAnalyticsData.setOption(position_analytics);

            if(value_analytics != null)
                buyAnalyticsData.setValue(value_analytics);

            buyAnalyticsData.setUuidIotrade(AppUtils.getUuid(AnalyticsActivity.this).toString());
            buyAnalyticsData.setUuidAnalyticsHub(analyticsPriceWrapper.getAnalyticsPrice().getUuid());

            EventBus.getDefault().post(buyAnalyticsData);
        }else {
            stopAnalyticsService();

            finishAnalyticsNoMatch();
        }
    }

    /**
     * Method that receives the response from the server with the set of services chosen
     *
     * @param response Object with the set of services chosen.
     */
    private void handleSensorChosenAnalytics(Response response) {
        currentTimeAfter = Calendar.getInstance().getTimeInMillis();
        diff = currentTimeAfter-currentTime;
        Log.d("Tempo Matchmaking (Ms)", String.valueOf(diff) + "ms");

        SensorPrice sensorPrice = response.getSensor();

        if( AppUtils.getUuid( this ) == null )
            AppUtils.createSaveUuid( this );

        if(sensorPrice != null) {
            keepCalculating = true;

            MatchmakingData msg = new MatchmakingData();
            msg.setUuidClient(response.getAnalytics().getUuid());
            msg.setUuidAnalyticsClient(AppUtils.getUuid(this).toString());
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
            analyticsPriceWrapper = new AnalyticsPriceWrapper(response.getAnalytics());

            BuyAnalyticsData buyAnalyticsData = new BuyAnalyticsData();
            buyAnalyticsData.setMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());
            buyAnalyticsData.setUuidData(sensorPriceWrapper.getSensorPrice().getUuidData());
            buyAnalyticsData.setOption(position_analytics);

            if(value_analytics != null)
                buyAnalyticsData.setValue(value_analytics);

            buyAnalyticsData.setUuidIotrade(AppUtils.getUuid(AnalyticsActivity.this).toString());
            buyAnalyticsData.setUuidAnalyticsHub(analyticsPriceWrapper.getAnalyticsPrice().getUuid());

            EventBus.getDefault().post(buyAnalyticsData);
        }
        else {
            keepRunning = false;
            finishAnalyticsNoMatch();
        }
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     * and starts a new matchmaking
     */
    private void handleResponseLostConnection(Response response) {
        switch (response.getMessage()){
            case "CON":
                connectionDisabled = true;
                break;
            case "ANA":
                analyticsDisabled = true;
                break;
        }

        if((!ackAnalytics && !ackConnection && analyticsDisabled && connectionDisabled) || (!ackConnection && ackAnalytics && connectionDisabled)){
            keepCalculating = false;

            MatchmakingData msg = new MatchmakingData();
            SensorPrice sensorPrice = sensorPriceWrapper.getSensorPrice();
            msg.setUuidClient(analyticsPriceWrapper.getAnalyticsPrice().getUuid());
            msg.setUuidAnalyticsClient(AppUtils.getUuid(this).toString());
            msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
            msg.setMacAddress(sensorPrice.getMacAdress());
            msg.setUuidData(sensorPrice.getUuidData());
            msg.setStartStop(MatchmakingData.STOP);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            currentTime = Calendar.getInstance().getTimeInMillis();

            SharedPreferences mSharedPreferences = getSharedPreferences(AppConfig.SHARED_PREF_FILE, MODE_PRIVATE);
            ObjectServer filter = new ObjectServer();

            Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
            Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

            Double lat = Double.parseDouble(mSharedPreferences.getString("latitude_current", "-500.0"));
            Double lng = Double.parseDouble(mSharedPreferences.getString("longitude_current", "-500.0"));

            if (latFixed == -500.0) {
                filter.setLat(lat);
                filter.setLng(lng);
            } else {
                filter.setLat(latFixed);
                filter.setLng(lngFixed);
            }

            filter.setRadius(mSharedPreferences.getFloat("radius", 1.5f));

            filter.setService(title.getText().toString());
            filter.setConnectionDevice(connectPriceWrapper.getConnectPrice().getDevice());

            connectionDisabled = false;
            analyticsDisabled = false;

            ackAnalytics = false;
            ackConnection = false;

            getSensorChosenAnalytics(filter);
        }else if(!ackAnalytics && ackConnection && analyticsDisabled){
            keepCalculating = false;

            currentTime = Calendar.getInstance().getTimeInMillis();

            ObjectServer filter = new ObjectServer();

            filter.setService(title.getText().toString());
            filter.setAnalyticsDevice(analyticsPriceWrapper.getAnalyticsPrice().getDevice());
            filter.setConnectionDevice(connectPriceWrapper.getConnectPrice().getDevice());
            filter.setSensorMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());

            connectionDisabled = false;
            analyticsDisabled = false;

            ackAnalytics = false;
            ackConnection = false;

            getChosenAnalytics(filter);
        }
    }

    /**
     * Method that receives the server response with updated information
     *
     * @param response Object with the provider returned by the server.
     */
    private void handleResponse(Response response) {
        currentPrice = response.getPrice();
        setProgress(false);
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     *
     */
    private void handleResponseUpdate(Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
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
     * Metódo creates a pop-up dialog to ask the user if they want to include the analytics service
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

    /**
     * Method creates a pop-up dialog to ask the user what value an alert will appear
     */
    private void createDialogAlert() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        int position = 1;
        EditText editText = customView.findViewById(R.id.editText);
        TextView text1 = customView.findViewById(R.id.text1);
        TextView buttonText1 = customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = customView.findViewById(R.id.buttonText2);

        text1.setText(getString(R.string.value));
        buttonText1.setText(getString(R.string.cancel));
        buttonText2.setText(getString(R.string.ok));

        customView.findViewById(R.id.text2).setVisibility(View.GONE);
        customView.findViewById(R.id.editText2).setVisibility(View.GONE);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(false);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editText.getText().toString().equals("")) {
                    BuyAnalyticsData buyAnalyticsData = new BuyAnalyticsData();
                    buyAnalyticsData.setMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());
                    buyAnalyticsData.setUuidData(sensorPriceWrapper.getSensorPrice().getUuidData());
                    buyAnalyticsData.setOption(position);
                    buyAnalyticsData.setValue(Double.valueOf(editText.getText().toString()));
                    buyAnalyticsData.setUuidIotrade(AppUtils.getUuid(AnalyticsActivity.this).toString());
                    buyAnalyticsData.setUuidAnalyticsHub(analyticsPriceWrapper.getAnalyticsPrice().getUuid());

                    EventBus.getDefault().post(buyAnalyticsData);

                    totalPrice += adapter.getItem(position).getPrice();
                    position_analytics = position;
                    value_analytics = Double.valueOf(editText.getText().toString());

                    Intent intent = new Intent(AnalyticsActivity.this, AnalyticsOptionActivity.class);

                    intent.putExtra("title_analytics", adapter.getItem(position).getTitle());
                    intent.putExtra("value_analytics", Double.valueOf(editText.getText().toString()));

                    clicked = true;

                    startActivityForResult(intent, 133);

                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused") // it's actually used to receive events from the Connection Service
    public void onEvent( String string ) {
        if(string != null){
            switch (string){
                case "a":
                    ackAnalytics = true;
                    break;
                case "c":
                    ackConnection = true;
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused") // it's actually used to receive events from the Connection Service
    public void onEvent( SendSensorData sendSensorData ) {
        if( sendSensorData != null && (sendSensorData.getData() == null && sendSensorData.getListData() == null)) {
            if(sendSensorData.getSource() == SendSensorData.MOBILE_HUB) {
                keepCalculating = false;

                MatchmakingData msg = new MatchmakingData();
                SensorPrice sensorPrice = sensorPriceWrapper.getSensorPrice();
                msg.setUuidClient(analyticsPriceWrapper.getAnalyticsPrice().getUuid());
                msg.setUuidAnalyticsClient(AppUtils.getUuid(this).toString());
                msg.setUuidMatch(connectPriceWrapper.getConnectPrice().getUuid());
                msg.setMacAddress(sensorPrice.getMacAdress());
                msg.setUuidData(sensorPrice.getUuidData());
                msg.setStartStop(MatchmakingData.STOP);

                msg.setRoute(ConnectionService.ROUTE_TAG);
                msg.setPriority(LocalMessage.HIGH);

                EventBus.getDefault().post(msg);

                currentTime = Calendar.getInstance().getTimeInMillis();

                SharedPreferences mSharedPreferences = getSharedPreferences(AppConfig.SHARED_PREF_FILE, MODE_PRIVATE);
                ObjectServer filter = new ObjectServer();

                Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
                Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

                Double lat = Double.parseDouble(mSharedPreferences.getString("latitude_current", "-500.0"));
                Double lng = Double.parseDouble(mSharedPreferences.getString("longitude_current", "-500.0"));

                if (latFixed == -500.0) {
                    filter.setLat(lat);
                    filter.setLng(lng);
                } else {
                    filter.setLat(latFixed);
                    filter.setLng(lngFixed);
                }

                filter.setRadius(mSharedPreferences.getFloat("radius", 1.5f));

                filter.setService(title.getText().toString());
                filter.setConnectionDevice(connectPriceWrapper.getConnectPrice().getDevice());

                getSensorChosenAnalytics(filter);
            }else {
                keepCalculating = false;

                currentTime = Calendar.getInstance().getTimeInMillis();

                ObjectServer filter = new ObjectServer();

                filter.setService(title.getText().toString());
                filter.setAnalyticsDevice(analyticsPriceWrapper.getAnalyticsPrice().getDevice());
                filter.setConnectionDevice(connectPriceWrapper.getConnectPrice().getDevice());
                filter.setSensorMacAddress(sensorPriceWrapper.getSensorPrice().getMacAdress());

                getChosenAnalytics(filter);
            }
        }else if( sendSensorData != null && (sendSensorData.getData() != null || sendSensorData.getListData() != null)) {
            lastTimeData = System.currentTimeMillis();
            intervalDisconnection = sendSensorData.getInterval();
        }
    }
}
