package com.lac.pucrio.luizpitta.iotrade.Activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.View;
import android.widget.TextView;

import com.infopae.model.SendSensorData;
import com.lac.pucrio.luizpitta.iotrade.Models.locals.EventData;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rx.subscriptions.CompositeSubscription;

/**
 * Classe Menu da aplicação, onde o usuário seleciona os parêmetros de sua conta na aplicação
 * que irão influenciar no algoritmo de marchmaking.
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsOptionActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Componentes de interface
     */
    private TextView dataText, title;

    /**
     * Variáveis
     */
    private CompositeSubscription mSubscriptions;
    private BroadcastReceiver mMessageReceiverFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /**
     * Método do sistema Android, chamado ao criar a Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_option);

        dataText = (TextView) findViewById(R.id.dataText);
        title = (TextView) findViewById(R.id.title);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }

        mSubscriptions = new CompositeSubscription();

        double value_analytics = getIntent().getDoubleExtra("value_analytics", -50000.0);
        if(value_analytics == -50000.0)
            title.setText(getIntent().getStringExtra("title_analytics"));
        else
            title.setText(getIntent().getStringExtra("title_analytics") + " > " + value_analytics);

        EventBus.getDefault().register( this );

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverFinish, new IntentFilter("finish_no_match"));
    }

    @Override //Finaliza a Activity
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
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
        if( sendSensorData != null && (sendSensorData.getData() != null || sendSensorData.getListData() != null) ) {
            String data = "";
            Double[] sensorData = sendSensorData.getData();
            ArrayList<Double[]> listData = sendSensorData.getListData();

            if((sensorData != null || (listData != null && listData.size() > 0))) {
                data = dataText.getText().toString() + getString(R.string.data) + "\n";
                if (sensorData == null) {
                    for (int i = 0; i < listData.size(); i++) {
                        Double[] d = listData.get(i);
                        data += Arrays.toString(d) + "\n";
                    }
                } else if(listData.size() > 0)
                    data += Arrays.toString(sensorData) + "\n";

                data += getString(R.string.date_time) + " " + Utilities.getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm a") + "\n\n";
                dataText.setText(data);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEvent( EventData eventData ) {
        if( eventData != null ) {

            // Event description
            Map<String, Object> payload = new HashMap<>();
            // Event data
            final String label = eventData.getLabel();
            final String data = eventData.getData();
            JsonReader reader = new JsonReader( new StringReader( data ) );

            try {
                reader.beginObject();
                while( reader.hasNext() ) {
                    final String name  = reader.nextName();
                    final JsonToken token = reader.peek();
                    Object value = null;

                    if( token.equals( JsonToken.STRING ) )
                        value = reader.nextString();
                    else if( token.equals( JsonToken.NUMBER ) )
                        value = reader.nextDouble();

                    payload.put( name, value );
                }
                reader.endObject();

            } catch( IOException e ) {
                e.printStackTrace();
            }


            String text = dataText.getText().toString() + getString(R.string.alert) + "\n";
            text += data + "\n";
            text += getString(R.string.date_time) + " " + Utilities.getDate(System.currentTimeMillis(), "dd/MM/yyyy hh:mm a") + "\n\n";
            dataText.setText(text);
        }
    }

    /**
     * Método do sistema Android, chamado ao ter interação do usuário com algum elemento de interface
     * @see View
     */
    @Override
    public void onClick(View view) {

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
     * Se {@code true}, então habilita a barra de progresso
     */
    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

}
