package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Services.ConnectionService;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppConfig;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Class Application menu, where the user selects the parameters of his account in the application
 * that will influence the algorithm of marchmaking.
 *
 * @author Luiz Guilherme Pitta
 */
public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     *  Interface Components
     */
    private EditText value, radius;
    private TextView confirmationButton, locationText, cleanText;

    private static final int PLACE_PICKER_REQUEST = 1020;
    private PlacePicker.IntentBuilder builder = null;
    private Intent placePicker = null;
    private Double lat = -500.0, lng = -500.0;

    /** Attributes */
    private CompositeSubscription mSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        value = (EditText) findViewById(R.id.value);
        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        locationText = (TextView) findViewById(R.id.locationText);
        cleanText = (TextView) findViewById(R.id.cleanText);
        radius = findViewById(R.id.radius);

        mSubscriptions = new CompositeSubscription();

        confirmationButton.setOnClickListener(this);
        locationText.setOnClickListener(this);
        cleanText.setOnClickListener(this);

        EventBus.getDefault().register( this );

        getUserInformation();

        builder = new PlacePicker.IntentBuilder();

        try {
            placePicker = builder.build(this);
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

    @Override
    public void onClick(View view) {
        if(view == confirmationButton)
        {
            SensorPrice sensorPrice = new SensorPrice();
            sensorPrice.setPrice(Double.valueOf(value.getText().toString()));
            updateUserBudget(sensorPrice);

            SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
            SharedPreferences.Editor editor = mSharedPreferences.edit();

            editor.putString("latitude", String.valueOf(lat));
            editor.putString("longitude", String.valueOf(lng));
            editor.putString("location", locationText.getText().toString());
            editor.putFloat("radius", Float.valueOf(radius.getText().toString())/1000.0f);
            editor.putFloat("price_target", Float.valueOf(value.getText().toString()));
            editor.apply();

        }else if (view == locationText) {
            startActivityForResult(placePicker, PLACE_PICKER_REQUEST);
        }else if (view == cleanText) {
            locationText.setText(null);

            SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
            SharedPreferences.Editor editor = mSharedPreferences.edit();

            editor.putString("latitude", null);
            editor.putString("longitude", null);
            editor.putString("location", null);
            lat = -500.0;
            lng = -500.0;
            editor.apply();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(MenuActivity.this, data);
                String name = selectedPlace.getAddress().toString();
                locationText.setText(name);
                lat = selectedPlace.getLatLng().latitude;
                lng = selectedPlace.getLatLng().longitude;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unused")
    @Subscribe()    // it's actually used to receive events from the Connection Service
    public void onEventMainThread( SensorPrice sensorPrice ) {
    }

    /**
     * Method that will make the request to the server to update user account parameters
     *
     * @param sensorPrice Object with the parameters to run the algorithm on the server.
     */
    private void updateUserBudget(SensorPrice sensorPrice) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateUserBudget(sensorPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUpdate,this::handleError));
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     *
     */
    private void handleResponseUpdate(Response response) {
        setProgress(false);
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Method that will make the request to the server to pick up the user account information
     *
     */
    private void getUserInformation() {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getUser()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Method that receives the response from the server with updated user information
     *
     *
     * @param response Object with the user returned by the server.
     */
    private void handleResponse(Response response) {

        value.setText(String.valueOf(response.getUser().getBudget()));

        SharedPreferences mSharedPreferences = getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat("price_target", Float.valueOf(value.getText().toString()));
        editor.apply();

        String location = mSharedPreferences.getString("location", "");

        if(!location.equals(""))
            locationText.setText(location);
        else
            locationText.setText(null);

        radius.setText(String.valueOf(mSharedPreferences.getFloat("radius", 1.5f)));



        setProgress(false);
    }

    /**
     * Method that receives the response from the server if an error has occurred
     *
     *
     * @param error Returns object with the error that occurred.
     */
    private void handleError(Throwable error) {
        setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
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
}
