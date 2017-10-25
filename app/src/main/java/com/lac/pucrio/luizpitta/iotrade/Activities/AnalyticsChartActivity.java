package com.lac.pucrio.luizpitta.iotrade.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.infopae.model.SendSensorData;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.lac.pucrio.luizpitta.iotrade.R;
import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import rx.subscriptions.CompositeSubscription;

/**
 * Class that receives the data from analytics provider and plot into a chart
 *
 * @author Luiz Guilherme Pitta
 */
public class AnalyticsChartActivity extends AppCompatActivity {

    /**
     * Interface Components
     */
    private TextView title;

    /** Attributes */
    private LineChartView chart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 9;
    private int numberOfPoints = 12;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = false;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor;
    private boolean hasGradientToTransparent = false;

    private float bottom, top, left, right;

    private CompositeSubscription mSubscriptions;
    private BroadcastReceiver mMessageReceiverFinish = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    /** Attributes */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_chart);

        chart = (LineChartView) findViewById(R.id.chart);
        title = (TextView) findViewById(R.id.title);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false); // disable the button
            actionBar.setDisplayHomeAsUpEnabled(false); // remove the left caret
            actionBar.setDisplayShowHomeEnabled(false); // remove the icon
        }


        chart.setOnValueTouchListener(new ValueTouchListener());

        mSubscriptions = new CompositeSubscription();

        title.setText(getIntent().getStringExtra("title_analytics"));

        EventBus.getDefault().register( this );
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverFinish, new IntentFilter("finish_no_match"));

        // Disable viewport recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        resetViewport();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();

        EventBus.getDefault().unregister( this );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Methods that initializes the chart
     *
     */
    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = numberOfPoints - 1;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    private void setViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = bottom;
        v.top = top;
        v.left = left;
        v.right = right;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    /**
     * Methods that receive and generate converts data to put into the chart
     *
     */
    private void generateValues(ArrayList<Double[]> listData) {
        numberOfPoints = listData.size();
        for (int i = 0; i < numberOfLines; i++) {
            for (int j = 0; j < numberOfPoints; j++) {
                Double[] data = listData.get(j);
                randomNumbersTab[i][j] = data[i].floatValue();
            }
        }
    }

    private void generateData() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; i++) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; j++) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            line.setHasGradientToTransparent(hasGradientToTransparent);
            if (pointsHaveDifferentColor){
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName(getString(R.string.time));
                axisY.setName("Axis Y");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")     // it's actually used to receive events from the Connection Service
    public void onEvent( SendSensorData sendSensorData ) {
        if( sendSensorData != null && sendSensorData.getListData() != null ) {
            ArrayList<Double[]> listData = sendSensorData.getListData();
            findEdgesChart(listData);
            numberOfLines = listData.get(0).length;
            numberOfPoints = listData.size();
            generateValues(listData);
            generateData();
        }
    }

    /**
     * Method that finds the edges in the chart
     *
     */
    private void findEdgesChart(ArrayList<Double[]> listData){
        ArrayList<Double> minMax = findMaxMin(listData);
        left = 0;
        right = listData.size() + 2;
        top = minMax.get(0).floatValue() + 5;
        bottom = minMax.get(1).floatValue() - 5;
        setViewport();
    }

    /**
     * Method that finds max and min values
     *
     */
    private ArrayList<Double> findMaxMin(ArrayList<Double[]> listData) {
        ArrayList<Double> minMax = new ArrayList<>();
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (int i=0;i<listData.size();i++){
            Double[] data = listData.get(i);
            for(double d : data) {
                if (d > max) max = d;
                if (d < min) min = d;
            }
        }

        minMax.add(min);
        minMax.add(max);

        return minMax;
    }


    /**
     * Chart TouchListener
     */
    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(AnalyticsChartActivity.this, "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }

    }

}
