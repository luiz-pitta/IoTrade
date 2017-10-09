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
import com.lac.pucrio.luizpitta.iotrade.Models.base.LocalMessage;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MatchmakingData;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.MessageData;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.Services.ConnectionService;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Classe principal da aplicação, onde tem toda a interação de receber e mandar os dados
 * do servidor conforme interação do usuário.
 *
 * @author Luiz Guilherme Pitta
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SwipeRefreshLayout.OnRefreshListener {

    /**
     * Componentes de interface
     */
    private TextView optionsButton, configButton, onButton;
    private EasyRecyclerView recyclerView;
    private SearchView searchView;
    private ServiceIoTAdapter adapter;

    /**
     * Variáveis
     */
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

    /**
     * Listner que é chamado ao usuário digitar algum caractere no campo de busca
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

    /**
     * Método do sistema Android, chamado ao criar a Activity
     */
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

                selectedCategory = adapter.getItem(position);

                if(!selectedCategory.contains("Atuar"))
                    createDialogAnalytics(filter);
                else
                    getSensorChosen(filter);
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

    /**
     * Método do sistema Android, chamado ao arrastar para atualizar a lista de categorias
     */
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
        }, 1000);

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

    /**
     * Método do sistema Android, chamado ao resumir a Activity
     */
    @Override
    public void onResume() {
        super.onResume();

        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        googleApiClient.connect();
    }

    /**
     * Método do sistema Android, chamado ao criar a Activity
     */
    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null)
            googleApiClient.connect();
    }

    /**
     * Método do sistema Android, chamado ao parar a Activity
     */
    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }

    /**
     * Método do sistema Android, chamado ao aprovar/rejeitar alguma permissão da aplicação
     */
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

    /**
     * Método do sistema Android, guarda o estado da aplicação para não ser destruido
     * pelo gerenciador de memória do sistema
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Método do sistema Android, chamado ao ter interação do usuário com algum elemento de interface
     * @see View
     */
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
     * @param query A query é usada para filtrar os resultados da lista de categorias.
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
     * Recebe a lista de categorias e a query para filtrar.
     * Itera pela lista e vai adicionando numa nova lista as categorias que contêm no nome
     * a palavra 'query'.
     *
     * @param models Lista de categorias.
     * @param query A palavra para filtragem.
     * @return Lista com as categorias filtradas.
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
     * Metódo que irá fazer a requisição ao servidor pela lista de categorias
     *
     * @param serviceIoT Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void getServices(ServiceIoT serviceIoT) {
        mSubscriptions.add(NetworkUtil.getRetrofit().getServices(serviceIoT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Metódo que recebe a resposta do servidor com a lista de categorias
     *
     *
     * @param response Objeto com a lista de categorias retornada pelo servidor.
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
     * Metódo que irá fazer a requisição ao servidor para rodar o algoritmo de matchmaking sem opção de analytics
     *
     * @param objectServer Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void getSensorChosen(ObjectServer objectServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit().getSensorAlgorithm(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosen,this::handleError));
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para rodar o algoritmo de matchmaking com opção de analytics
     *
     * @param objectServer Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void getSensorChosenAnalytics(ObjectServer objectServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit().getSensorAlgorithmAnalytics(objectServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosenAnalytics,this::handleError));
    }

    /**
     * Metódo que recebe a resposta do servidor com o conjunto de serviços escolhidos
     *
     *
     * @param response Objeto com o conjunto de serviços escolhidos.
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
            msg.setUuidAnalyticsClient(AppUtils.getUuid(this).toString());
            msg.setUuidMatch(response.getConnect().getUuid());
            msg.setMacAddress(response.getSensor().getMacAdress());
            msg.setUuidData(response.getSensor().getUuidData());
            msg.setStartStop(MatchmakingData.START);

            msg.setRoute(ConnectionService.ROUTE_TAG);
            msg.setPriority(LocalMessage.HIGH);

            EventBus.getDefault().post(msg);

            intent = new Intent(this, AnalyticsActivity.class);

            intent.putExtra("category", selectedCategory);
            intent.putExtra("sensor_price", new SensorPriceWrapper(response.getSensor()));
            intent.putExtra("connect_price", new ConnectPriceWrapper(response.getConnect()));
            intent.putExtra("analytics_price", new AnalyticsPriceWrapper(response.getAnalytics()));
            startActivity(intent);
        }
        else
            Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();

        recyclerView.showRecycler();
    }

    /**
     * Metódo que recebe a resposta do servidor com o conjunto de serviços escolhidos
     *
     *
     * @param response Objeto com o conjunto de serviços escolhidos.
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

            if(!response.getSensor().isActuator()) {
                MatchmakingData msg = new MatchmakingData();
                msg.setUuidClient(AppUtils.getUuid(this).toString());
                msg.setUuidMatch(response.getConnect().getUuid());
                msg.setMacAddress(response.getSensor().getMacAdress());
                msg.setUuidData(response.getSensor().getUuidData());
                msg.setStartStop(MatchmakingData.START);

                msg.setRoute(ConnectionService.ROUTE_TAG);
                msg.setPriority(LocalMessage.HIGH);

                EventBus.getDefault().post(msg);

                intent = new Intent(this, ResultActivity.class);
            }
            else
                intent = new Intent(this, ActuatorActivity.class);

            intent.putExtra("category", selectedCategory);
            intent.putExtra("sensor_price", new SensorPriceWrapper(response.getSensor()));
            intent.putExtra("connect_price", new ConnectPriceWrapper(response.getConnect()));
            intent.putExtra("analytics_price", new AnalyticsPriceWrapper(response.getAnalytics()));
            startActivity(intent);
        }
        else
            Toast.makeText(this, getResources().getString(R.string.no_sensors_available), Toast.LENGTH_LONG).show();

        recyclerView.showRecycler();
    }

    /**
     * Metódo que recebe a resposta do servidor caso tenha ocorrido um erro
     *
     *
     * @param error Retorna objeto com o erro que ocorreu.
     */
    private void handleError(Throwable error) {
        setProgress(false);
        recyclerView.showRecycler();
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    /**
     * Metódo cria um diálogo pop-up para perguntar ao usuário se deseja incluir o serviço do analytics
     * ao escolher uma categoria
     *
     * @param sensor Objeto com os parametros para mandar ao servidor.
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
                currentTime = Calendar.getInstance().getTimeInMillis();
                getSensorChosen(sensor);
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTime = Calendar.getInstance().getTimeInMillis();
                getSensorChosenAnalytics(sensor);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @SuppressWarnings("unused")
    @Subscribe()
    public void onEventMainThread( SensorPrice sensorPrice ) {

    }

    /**
     * Método do sistema Android, chamado ao detectar mudança de localização pelo GPS
     */
    @Override
    public void onLocationChanged(Location lastLocation) {
        lat = lastLocation.getLatitude();
        lng = lastLocation.getLongitude();
    }

    /**
     * Método do sistema Android, chamado ao detectar falha de localização do GPS
     */
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {}

    /**
     * Método do sistema Android, chamado ao detectar suspensão na chamada do GPS
     */
    @Override
    public void onConnectionSuspended(int cause) {}

    /**
     * Método do sistema Android, chamado ao se conectar ao GPS
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                lat = lastLocation.getLatitude();
                lng = lastLocation.getLongitude();

                SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );

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
}