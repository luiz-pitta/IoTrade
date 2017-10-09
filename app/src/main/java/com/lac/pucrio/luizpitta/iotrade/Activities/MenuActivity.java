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
 * Classe Menu da aplicação, onde o usuário seleciona os parêmetros de sua conta na aplicação
 * que irão influenciar no algoritmo de marchmaking.
 *
 * @author Luiz Guilherme Pitta
 */
public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Componentes de interface
     */
    private EditText value, radius;
    private TextView confirmationButton, locationText, cleanText;

    private static final int PLACE_PICKER_REQUEST = 1020;
    private PlacePicker.IntentBuilder builder = null;
    private Intent placePicker = null;
    private Double lat = -500.0, lng = -500.0;

    /**
     * Variáveis
     */
    private CompositeSubscription mSubscriptions;

    /**
     * Método do sistema Android, chamado ao criar a Activity
     */
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

    /**
     * Método do sistema Android, chamado ao destruir a Activity
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

    @SuppressWarnings("unused")
    @Subscribe()
    public void onEventMainThread( SensorPrice sensorPrice ) {
        Toast.makeText(this, "Passei", Toast.LENGTH_LONG).show();
    }

    /**
     * Método do sistema Android, chamado ao ter interação do usuário com algum elemento de interface
     * @see View
     */
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

    /**
     * Método do sistema Android, guarda o estado da aplicação para não ser destruido
     * pelo gerenciador de memória do sistema
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para atualizar parametros de conta do usuário
     *
     * @param sensorPrice Objeto com os parametros para rodar o algoritmo no servidor.
     */
    private void updateUserBudget(SensorPrice sensorPrice) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateUserBudget(sensorPrice)
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
        setProgress(false);
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Metódo que irá fazer a requisição ao servidor para pegar as informações de conta do usuário
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
     * Metódo que recebe a resposta do servidor com as informações do usuário atualizado
     *
     *
     * @param response Objeto com o usuário retornado pelo servidor.
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
}
