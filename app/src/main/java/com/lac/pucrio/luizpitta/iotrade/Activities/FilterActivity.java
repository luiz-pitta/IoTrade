package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.lac.pucrio.luizpitta.iotrade.Adapters.ServiceIoTAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.FilterServer;
import com.lac.pucrio.luizpitta.iotrade.Models.FilterWrapper;
import com.lac.pucrio.luizpitta.iotrade.Models.ServiceIoT;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import java.util.ArrayList;

import rx.subscriptions.CompositeSubscription;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener {

    private CompositeSubscription mSubscriptions;
    private Handler handler = new Handler();

    private TextView confirmationButton;
    private EditText priceStart, priceEnd, CategoryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        mSubscriptions = new CompositeSubscription();

        confirmationButton = (TextView) findViewById(R.id.confirmationButton);
        priceStart = (EditText) findViewById(R.id.priceStart);
        priceEnd = (EditText) findViewById(R.id.priceEnd);
        CategoryText = (EditText) findViewById(R.id.CategoryText);

        confirmationButton.setText(getResources().getString(R.string.confirm));

        confirmationButton.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        if(view == confirmationButton){
            FilterServer filterServer = new FilterServer();
            filterServer.setQuery(!CategoryText.getText().toString().matches("") ? CategoryText.getText().toString() : "");
            filterServer.setFrom(!priceStart.getText().toString().matches("") ? Double.valueOf(priceStart.getText().toString()) : 0);
            filterServer.setTo(!priceEnd.getText().toString().matches("") ? Double.valueOf(priceEnd.getText().toString()) : 0);

            FilterWrapper filterWrapper = new FilterWrapper(filterServer);

            Intent intent = new Intent();
            intent.putExtra("filter_att", filterWrapper);
            setResult(RESULT_OK, intent);
            finish();
        }

    }
}
