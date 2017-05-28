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
import com.lac.pucrio.luizpitta.iotrade.Activities.FilterActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.MenuActivity;
import com.lac.pucrio.luizpitta.iotrade.Activities.OptionsActivity;
import com.lac.pucrio.luizpitta.iotrade.Adapters.ServiceIoTAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.FilterServer;
import com.lac.pucrio.luizpitta.iotrade.Models.FilterWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.Utils.Constants;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SwipeRefreshLayout.OnRefreshListener {

    private TextView optionsButton, filterButton, configButton;
    private EasyRecyclerView recyclerView;
    private SearchView searchView;
    private ArrayList<ServiceIoT> serviceIoTQueryList = new ArrayList<>(), serviceIoTList = new ArrayList<>();
    private CompositeSubscription mSubscriptions;
    private Handler handler = new Handler();
    private ServiceIoTAdapter adapter;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double lat = -500, lng = -500;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private FilterServer filterServer = null;

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
        filterButton = (TextView) findViewById(R.id.filterButton);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        configButton.setOnClickListener(this);
        filterButton.setOnClickListener(this);
        optionsButton.setOnClickListener(this);

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
                FilterServer filter = new FilterServer();

                filter.setLat(lat);
                filter.setLng(lng);

                filter.setService(adapter.getItem(position).getTitle());

                getSensorChosen(filter);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

        recyclerView.showProgress();
    }

    @Override
    public void onRefresh() {

        adapter.clear();
        recyclerView.showProgress();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ServiceIoT serviceIoT = new ServiceIoT();
                serviceIoT.setLat(lat);
                serviceIoT.setLng(lng);

                getServices(serviceIoT);
            }
        }, 1000);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        googleApiClient.connect();
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
        }else if(view == filterButton) {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            startActivityForResult(intent, Constants.FILTER_RESULT);
        }else if(view == optionsButton){
            startActivity(new Intent(MainActivity.this, OptionsActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.FILTER_RESULT) {

            FilterWrapper wrap =
                    (FilterWrapper) data.getSerializableExtra("filter_att");

            filterServer = wrap.getFilterServer();

            filterServer.setLat(lat);
            filterServer.setLng(lng);

            getServicesFilter(filterServer);
        }
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<ServiceIoT> filteredModelList = filter(serviceIoTList, query);
                adapter.swap(filteredModelList);
                recyclerView.scrollToPosition(0);
                setProgress(false);
            }
        });
        // Load complete
    }

    private ArrayList<ServiceIoT> filter(ArrayList<ServiceIoT> models, String query) {
        String lowerCaseQuery = query.toLowerCase();

        ArrayList<ServiceIoT> filteredModelList = new ArrayList<>();
        for (ServiceIoT model : models) {
            final String text = model.getTitle().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void getServices(ServiceIoT serviceIoT) {
        mSubscriptions.add(NetworkUtil.getRetrofit().getServices(serviceIoT)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void getServicesFilter(FilterServer filterServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit().getServicesFilter(filterServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        adapter.clear();
        serviceIoTQueryList.clear();
        serviceIoTList.clear();

        serviceIoTQueryList.addAll(response.getServices().size() == 0 ? new ArrayList<>() : response.getServices());
        serviceIoTList.addAll(serviceIoTQueryList);

        adapter.addAll(serviceIoTQueryList);
    }

    private void getSensorChosen(FilterServer filterServer) {
        recyclerView.showProgress();
        mSubscriptions.add(NetworkUtil.getRetrofit().getSensorAlgorithm(filterServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleSensorChosen,this::handleError));
    }

    private void handleSensorChosen(Response response) {
        createDialogRating(response.getSensor());
        recyclerView.showRecycler();
    }

    private void updateSensorInformation(SensorPrice sensorPrice) {
        mSubscriptions.add(NetworkUtil.getRetrofit().updateSensorRating(sensorPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUpdate,this::handleError));
    }

    private void handleResponseUpdate(Response response) {
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void handleError(Throwable error) {
        setProgress(false);
        recyclerView.showRecycler();
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onLocationChanged(Location lastLocation) {
        lat = lastLocation.getLatitude();
        lng = lastLocation.getLongitude();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                lat = lastLocation.getLatitude();
                lng = lastLocation.getLongitude();

                ServiceIoT serviceIoT = new ServiceIoT();
                serviceIoT.setLat(lat);
                serviceIoT.setLng(lng);

                if(filterServer == null)
                    getServices(serviceIoT);

                locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(5000); //TODO mudar no app final
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }
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

    private void createDialogRating(SensorPrice sensorPrice) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_rating, null);


        SmileRating mSmileRating = (SmileRating) customView.findViewById(R.id.ratingView);
        TextView sensorChosen = (TextView) customView.findViewById(R.id.sensorChosen);

        mSmileRating.setNameForSmile(BaseRating.TERRIBLE, "1");
        mSmileRating.setNameForSmile(BaseRating.BAD, "2");
        mSmileRating.setNameForSmile(BaseRating.OKAY, "3");
        mSmileRating.setNameForSmile(BaseRating.GOOD, "4");
        mSmileRating.setNameForSmile(BaseRating.GREAT, "5");

        sensorChosen.setText(sensorPrice.getTitle() + " - " + sensorPrice.getCategory() + " - " + sensorPrice.getPrice());

        mSmileRating.setSelectedSmile(BaseRating.GREAT);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        mSmileRating.setOnRatingSelectedListener(new SmileRating.OnRatingSelectedListener() {
            @Override
            public void onRatingSelected(int level, boolean reselected) {
                sensorPrice.setRank(level);
                updateSensorInformation(sensorPrice);
                dialog.dismiss();
            }
        });

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();
    }
}
