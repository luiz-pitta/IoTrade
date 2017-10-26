package com.lac.pucrio.luizpitta.iotrade;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.lac.pucrio.luizpitta.iotrade.Activities.ActuatorActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.AnalyticsActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.AnalyticsChartActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.MenuActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.OptionsActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.ResultActivity;
import com.lac.pucrio.luizpitta.iotrade.Adapters.ServiceIoTAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.AnalyticsPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ConnectPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ObjectServer;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPriceWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.Models.User;
import com.lac.pucrio.luizpitta.iotrade.Models.base.LocalMessage;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MatchmakingData;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.Services.ConnectionService;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Main application class, where you have all the interaction of receiving and sending data
 * of the server according to user interaction.
 *
 * @author Luiz Guilherme Pitta
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SwipeRefreshLayout.OnRefreshListener {

    /**
     * Interface Components
     */
    private TextView optionsButton, configButton, onButton;
    private EasyRecyclerView recyclerView;
    private SearchView searchView;
    private ServiceIoTAdapter adapter;

    /** Attributes */
    private CompositeSubscription mSubscriptions;
    private Handler handler = new Handler();
    private ArrayList<String> serviceIoTQueryList = new ArrayList<>(), serviceIoTList = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    //private double lat = -500, lng = -500;
    private double lat = -22.92484013, lng = -43.25909615;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private long currentTime, currentTimeAfter, diff;
    private String selectedCategory = "";
    private boolean ackConnection = false, ackAnalytics = false;
    private boolean connectionDisabled = false, analyticsDisabled = false;

    /**
     * Listener that is called to the user to type some character in the search field
     * @see #executeFilter(String)
     */
    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            setProgress(true);
            executeFilter(query);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubscriptions = new CompositeSubscription();

        searchView = (SearchView) findViewById(R.id.searchSelectionView);
        optionsButton = (TextView) findViewById(R.id.optionsButton);
        configButton = (TextView) findViewById(R.id.configButton);
        onButton = (TextView) findViewById(R.id.onButton);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        configButton.setOnClickListener(this);
        optionsButton.setOnClickListener(this);
        onButton.setOnClickListener(this);

        //search bar
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
        //search bar end

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this, R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServiceIoTAdapter(this);

        recyclerView.setEmptyView(R.layout.empty_list);

        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColor(ContextCompat.getColor(this,R.color.colorAccent));
        recyclerView.setAdapterWithProgress(adapter);

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
                ObjectServer filter = new ObjectServer();

                Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
                Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

                if(latFixed == -500.0) {
                    filter.setLat(lat);
                    filter.setLng(lng);
                }else {
                    filter.setLat(latFixed);
                    filter.setLng(lngFixed);
                }
                filter.setRadius(mSharedPreferences.getFloat("radius", 1.5f));

                filter.setService(adapter.getItem(position));
                filter.setConnectionDevice("");

                selectedCategory = adapter.getItem(position);

                if(!selectedCategory.contains("Atuar"))
                    createDialogAnalytics(filter);
                else {
                    setProgress(true);
                    getSensorChosen(filter);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        Intent iConn = new Intent(this, ConnectionService.class);

        if (AppUtils.isMyServiceRunning(this, ConnectionService.class.getName()))
            stopService(iConn);

        recyclerView.showRecycler();

        EventBus.getDefault().register( this );
    }

    @Override
    public void onRefresh() {

        adapter.clear();
        recyclerView.showProgress();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );

                Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
                Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

                ServiceIoT serviceIoT = new ServiceIoT();

                if(latFixed == -500.0) {
                    serviceIoT.setLat(lat);
                    serviceIoT.setLng(lng);
                }else {
                    serviceIoT.setLat(latFixed);
                    serviceIoT.setLng(lngFixed);
                }

                serviceIoT.setRadius(mSharedPreferences.getFloat("radius", 1.5f));

                getServices(serviceIoT);
            }
        }, 500);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (AppUtils.isMyServiceRunning(this, ConnectionService.class.getName()))
            onButton.setText(getResources().getString(R.string.off));
        else
            onButton.setText(getResources().getString(R.string.on));


        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        googleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null)
            googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, getResources().getString(R.string.location_permission), Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == configButton){
            startActivity(new Intent(MainActivity.this, MenuActivity.class));
        }else if(view == optionsButton){
            startActivity(new Intent(MainActivity.this, OptionsActivity.class));
        }else if(view == onButton){
            String text = onButton.getText().toString();
            Intent iConn = new Intent(this, ConnectionService.class);
            if(text.equals("ON")) {
                onButton.setText(getResources().getString(R.string.off));
                if (AppUtils.isMyServiceRunning(this, ConnectionService.class.getName()))
                    stopService(iConn);

                startService(iConn);
            }else if(text.equals("OFF")) {
                onButton.setText(getResources().getString(R.string.on));
                stopService(iConn);
            }

        }
    }

    @Override
    public void onLocationChanged(Location lastLocation) {
        lat = lastLocation.getLatitude();
        lng = lastLocation.getLongitude();

        SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putString("latitude_current", String.valueOf(lat));
        editor.putString("longitude_current", String.valueOf(lng));
        editor.apply();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {}

    @Override
    public void onConnectionSuspended(int cause) {}

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                lat = lastLocation.getLatitude();
                lng = lastLocation.getLongitude();

                SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
                SharedPreferences.Editor editor = mSharedPreferences.edit();

                editor.putString("latitude_current", String.valueOf(lat));
                editor.putString("longitude_current", String.valueOf(lng));
                editor.apply();

                ServiceIoT serviceIoT = new ServiceIoT();

                Double latFixed = Double.parseDouble(mSharedPreferences.getString("latitude", "-500.0"));
                Double lngFixed = Double.parseDouble(mSharedPreferences.getString("longitude", "-500.0"));

                if(latFixed == -500.0) {
                    serviceIoT.setLat(lat);
                    serviceIoT.setLng(lng);
                }else {
                    serviceIoT.setLat(latFixed);
                    serviceIoT.setLng(lngFixed);
                }
                serviceIoT.setRadius(mSharedPreferences.getFloat("radius", 1.5f));

                getServices(serviceIoT);

                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(2000); //TODO mudar no app final
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }
    }

    /**
     * If {@code true}, enable the progress bar
     */
    public void setProgress(boolean progress) {
        if(progress) {
            findViewById(R.id.progressBox).bringToFront();
            findViewById(R.id.progressBox).invalidate();
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.progressBox).setVisibility(View.GONE);
            findViewById(R.id.progressBox).invalidate();
        }
    }

    /**
     * @param query The query is used to filter the results of the category list.
     */
    public void executeFilter(String query) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> filteredModelList = filter(serviceIoTList, query);
                adapter.swap(filteredModelList);
                recyclerView.scrollToPosition(0);
                setProgress(false);
            }
        });
    }

    /**
     * Receives list of categories and query to filter.
     * Iterates by the list and is adding in a new list the categories that contain in the name
     * the word 'query'.
     *
     * @param models Category list.
     * @param query The word for filtering.
     * @return List with filtered categories.
     */
    private ArrayList<String> filter(ArrayList<String> models, String query) {
        String lowerCaseQuery = query.toLowerCase();

        ArrayList<String> filteredModelList = new ArrayList<>();
        for (String model : models) {
            final String text = model.toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    /**
     * Method that will make the request to the server through the list of categories
     *
     * @param serviceIoT Object with the parameters to run the algorithm on the server.
     */
    private void getServices(ServiceIoT serviceIoT) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getServices(serviceIoT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Method that receives the response from the server with the list of categories
     *
     *
     * @param response Object with the list of categories returned by the server.
     */
    private void handleResponse(Response response) {
        adapter.clear();
        serviceIoTQueryList.clear();
        serviceIoTList.clear();

        serviceIoTQueryList.addAll(response.getCategories().size() == 0 ? new ArrayList<>() : response.getCategories());
        serviceIoTList.addAll(serviceIoTQueryList);

        adapter.addAll(serviceIoTQueryList);
    }

    /**
     * Method that will make the request to the server to run the matchmaking algorithm without analytics option
     *
     * @param objectServer Object with the parameters to run the algorithm on the server.
     */
    private void getSensorChosen(ObjectServer objectServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getSensorAlgorithm(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosen,this::handleError));
    }

    /**
     * The method used to logout analytics user.
     */
    private void setAnalyticsHubDisabled(AnalyticsPrice analyticsPrice) {
        User user = new User();
        user.setUuid(UUID.fromString(analyticsPrice.getUuid()));
        user.setDevice(analyticsPrice.getDevice());
        user.setActive(false);

        registerAnalytics(user);
    }

    /**
     * The method used to register state in server of analytics provider user.
     * @param usr The analytics provider user.
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
    private void setMobileHubDisabled(ConnectPrice connectPrice) {
        User user = new User();
        user.setUuid(UUID.fromString(connectPrice.getUuid()));
        user.setDevice(connectPrice.getDevice());
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
     * Method that receives the response from the server
     *
     */
    private void handleResponseLostConnection(Response response) {
        ackConnection = false;
        ackAnalytics = false;
        analyticsDisabled = false;
        connectionDisabled = false;
    }

    /**
     * Method that will make the request to the server to run the matchmaking algorithm with analytics option
     *
     * @param objectServer Object with the parameters to run the algorithm on the server.
     */
    private void getSensorChosenAnalytics(ObjectServer objectServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getSensorAlgorithmAnalytics(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosenAnalytics,this::handleError));
    }

    /**
     * Method that receives the response from the server with the set of services chosen
     *
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
            Intent intent;

            MatchmakingData msg = new MatchmakingData();
            msg.setUuidClient(response.getAnalytics().getUuid());
            msg.setUuidAnalyticsClient(AppUtils.getUuid(MainActivity.this).toString());
            msg.setUuidMatch(response.getConnect().getUuid());
            msg.setMacAddress(response.getSensor().getMacAdress());
            msg.setUuidData(response.getSensor().getUuidData());
            msg.setAck(true);
            msg.setStartStop(MatchmakingData.START);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            intent = new Intent(this, AnalyticsActivity.class);
            intent.putExtra("category", selectedCategory);
            intent.putExtra("sensor_price", new SensorPriceWrapper(response.getSensor()));
            intent.putExtra("connect_price", new ConnectPriceWrapper(response.getConnect()));
            intent.putExtra("analytics_price", new AnalyticsPriceWrapper(response.getAnalytics()));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ackAnalytics && ackConnection) {
                        ackConnection = false;
                        ackAnalytics = false;

                        setProgress(false);

                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.try_again), Toast.LENGTH_LONG).show();

                        setProgress(false);

                        if(!ackAnalytics)
                            setAnalyticsHubDisabled(response.getAnalytics());

                        if(!ackConnection)
                            setMobileHubDisabled(response.getConnect());
                    }

                }
            }, Constants.ACK_TIMEOUT_MAIN);

        }
        else
            Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();

        recyclerView.showRecycler();
    }

    /**
     * Method that receives the response from the server with the set of services chosen
     *
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
            Intent intent;

            MatchmakingData msg = new MatchmakingData();
            msg.setUuidClient(AppUtils.getUuid(this).toString());
            msg.setUuidMatch(response.getConnect().getUuid());
            msg.setMacAddress(response.getSensor().getMacAdress());
            msg.setUuidData(response.getSensor().getUuidData());
            msg.setAck(true);
            msg.setStartStop(MatchmakingData.START);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            if(!response.getSensor().isActuator())
                intent = new Intent(this, ResultActivity.class);
            else
                intent = new Intent(this, ActuatorActivity.class);

            intent.putExtra("category", selectedCategory);
            intent.putExtra("sensor_price", new SensorPriceWrapper(response.getSensor()));
            intent.putExtra("connect_price", new ConnectPriceWrapper(response.getConnect()));
            intent.putExtra("analytics_price", new AnalyticsPriceWrapper(response.getAnalytics()));

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(ackConnection) {
                        ackConnection = false;

                        setProgress(false);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.try_again), Toast.LENGTH_LONG).show();

                        setProgress(false);
                        setMobileHubDisabled(response.getConnect());
                    }


                }
            }, Constants.ACK_TIMEOUT_MAIN);
        }
        else
            Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();

        recyclerView.showRecycler();
    }

    /**
     * Method that receives the response from the server if an error has occurred
     *
     * @param error Returns object with the error that occurred.
     */
    private void handleError(Throwable error) {
        setProgress(false);
        recyclerView.showRecycler();
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    /**
     * Metódo creates a pop-up dialog to ask the user if they want to include the analytics service
     * when choosing a category
     *
     * @param sensor Object with the parameters to send to the server.
     */
    private void createDialogAnalytics(ObjectServer sensor) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        EditText editText2 = (EditText) customView.findViewById(R.id.editText2);

        editText.setVisibility(View.GONE);
        editText2.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        text1.setText(getResources().getString(R.string.analytics_service_dialog_text));
        buttonText1.setText(getResources().getString(R.string.no));
        buttonText2.setText(getResources().getString(R.string.yes));

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgress(true);
                currentTime = Calendar.getInstance().getTimeInMillis();
                getSensorChosen(sensor);
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProgress(true);
                currentTime = Calendar.getInstance().getTimeInMillis();
                getSensorChosenAnalytics(sensor);
                dialog.dismiss();
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
}