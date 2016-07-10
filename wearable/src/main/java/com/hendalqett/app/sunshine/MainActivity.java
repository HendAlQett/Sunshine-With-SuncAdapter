package com.hendalqett.app.sunshine;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import modules.Weather;
import observers.BusProvider;


public class MainActivity extends WearableActivity  {


    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);
    private static final SimpleDateFormat HOUR_DATE_FORMAT =
            new SimpleDateFormat("HH", Locale.US);
    private static final SimpleDateFormat MINUTES_DATE_FORMAT =
            new SimpleDateFormat(":mm", Locale.US);


    private BoxInsetLayout mContainerView;
    private TextView mClockHour;
    private TextView mClockMin;
    private TextView mDate;
    private TextView mMaxView;
    private TextView mMinView;
    private ImageView mWeatherIcon;
    String dateFull;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setAmbientEnabled();
        dateFull = new SimpleDateFormat("E, MMM d, yyyy").format(new Date());

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mDate = (TextView) findViewById(R.id.tvDate);
        mClockHour = (TextView) findViewById(R.id.clockHour);
        mClockMin = (TextView) findViewById(R.id.clockMin);
        mMaxView = (TextView) findViewById(R.id.tvMax);
        mMinView = (TextView) findViewById(R.id.tvMin);
        mWeatherIcon = (ImageView) findViewById(R.id.ivWeather);

        updateDisplay();
    }


    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);

    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {

        mDate.setText(dateFull);
        mClockHour.setText(HOUR_DATE_FORMAT.format(new Date()));
        mClockMin.setText(MINUTES_DATE_FORMAT.format(new Date()));
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));

        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.primary));
        }


    }

    @Subscribe
    public void updateWeather(Weather weather) {

        Log.d("DataSent","updating");
        mMaxView.setText(weather.getMax());
        mMinView.setText(weather.getMin());
        mWeatherIcon.setImageBitmap(weather.getBitmap());
    }


}
