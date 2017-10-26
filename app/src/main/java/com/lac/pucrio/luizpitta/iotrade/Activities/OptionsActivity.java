package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.lac.pucrio.luizpitta.iotrade.Adapters.SensorPriceAdapter;
import com.lac.pucrio.luizpitta.iotrade.Models.Response;
import com.lac.pucrio.luizpitta.iotrade.Models.SensorPrice;
import com.lac.pucrio.luizpitta.iotrade.Network.NetworkUtil;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Class where sensors registered in the application are presented, where administrator
 * can change their values
 *
 * @author Luiz Guilherme Pitta
 */
public class OptionsActivity extends AppCompatActivity {

    /**
     * Interface Components
     */
    private EasyRecyclerView recyclerView;

    /** Attributes */
    private CompositeSubscription mSubscriptions;
    private SensorPriceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this, R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SensorPriceAdapter(this);

        adapter.addAll(new ArrayList<SensorPrice>());
        recyclerView.setAdapterWithProgress(adapter);

        recyclerView.setEmptyView(R.layout.empty_list);

        mSubscriptions = new CompositeSubscription();

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                createDialogUpdate(position);
            }
        });

        getSensorInformation();
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

    /**
     * Method that will make the request to the server to update parameters of the sensors
     *
     * @param sensorPrice Object with the parameters to run the algorithm on the server.
     */
    private void updateSensorInformation(SensorPrice sensorPrice) {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).updateSensorInformation(sensorPrice)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUpdate,this::handleError));
    }

    /**
     * Method that receives the response from the server if everything has run correctly
     *
     */
    private void handleResponseUpdate(Response response) {
        getSensorInformation();
        Toast.makeText(this, response.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Method that will make the request to the server to retrieve information from the sensors
     *
     */
    private void getSensorInformation() {
        mSubscriptions.add(NetworkUtil.getRetrofit(this).getSensorPrice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Method that receives the response from the server with the sensor information
     *
     *
     * @param response Object with the list of sensors returned by the server.
     */
    private void handleResponse(Response response) {
        adapter.clear();
        adapter.addAll(response.getSensorPrice());
    }

    /**
     * Method that receives the response from the server if an error has occurred.
     *
     * @param error Returns object with the error that occurred.
     */
    private void handleError(Throwable error) {
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    /**
     * Method creates a pop-up dialog to ask the administrator what information he wants
     * change to certain sensor
     *
     * @param item position in the list.
     */
    private void createDialogUpdate(int item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);
        EditText editText2 = (EditText) customView.findViewById(R.id.editText2);

        Dialog dialog = new Dialog(this, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        SensorPrice sensorPrice = adapter.getItem(item);
        editText.setText(String.valueOf(sensorPrice.getPrice()));
        editText2.setText(sensorPrice.getCategory());

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorPrice sensorPrice1 = new SensorPrice();
                sensorPrice1.setTitle(sensorPrice.getTitle());
                sensorPrice1.setCategory(sensorPrice.getCategory());
                sensorPrice1.setCategoryNew(editText2.getText().toString());
                sensorPrice1.setPrice(Double.valueOf(editText.getText().toString()));
                updateSensorInformation(sensorPrice1);
                recyclerView.showProgress();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
