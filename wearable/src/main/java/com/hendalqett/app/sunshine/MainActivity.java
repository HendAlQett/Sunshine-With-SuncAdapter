package com.hendalqett.app.sunshine;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
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
    private static final String WEATHER_MAX_KEY = "weather_max";
    private static final String WEATHER_MIN_KEY = "weather_min";
    private static final String WEATHER_IMAGE_KEY = "weather_photo";
//    private GoogleApiClient mGoogleApiClient;

    private BoxInsetLayout mContainerView;
    private TextView mClockMin;
    private TextView mClockSec;
    private TextView mDate;
    private TextView mMaxView;
    private TextView mMinView;
    private ImageView mWeatherIcon;
    String dateFull;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();

        setAmbientEnabled();
        dateFull = new SimpleDateFormat("E, MMM d, yyyy").format(new Date());

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mDate = (TextView) findViewById(R.id.tvDate);
        mClockMin = (TextView) findViewById(R.id.clockMin);
        mClockSec = (TextView) findViewById(R.id.clockSec);
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
//        mClockMin.setVisibility(View.VISIBLE);
//        mClockSec.setVisibility(View.VISIBLE);
//        mClockMin.setTextColor(getResources().getColor(android.R.color.white));
//        mClockSec.setTextColor(getResources().getColor(android.R.color.white));
        mClockMin.setText(HOUR_DATE_FORMAT.format(new Date()));
        mClockSec.setText(MINUTES_DATE_FORMAT.format(new Date()));
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//            mClockView.setTextColor(getResources().getColor(android.R.color.white));


//            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));

//            mClockView.setText(HOUR_DATE_FORMAT.format(new Date())+MINUTES_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.primary));

//            mClockView.setVisibility(View.VISIBLE);
//            mClockView.setText(HOUR_DATE_FORMAT.format(new Date())+MINUTES_DATE_FORMAT.format(new Date()));
        }


    }

    @Subscribe
    public void updateWeather(Weather weather) {

        mMaxView.setText(weather.getMax());
        mMinView.setText(weather.getMin());
        mWeatherIcon.setImageBitmap(weather.getBitmap());
    }

//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        for (DataEvent dataEvent : dataEvents)
//        {
//            if (dataEvent.getType()==DataEvent.TYPE_CHANGED)
//            {
//                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
//                String path = dataEvent.getDataItem().getUri().getPath();
//                if (path.equals("/com.hend.weather"))
//                {
//                    String max = dataMap.getString(WEATHER_MAX_KEY);
//                    String min = dataMap.getString(WEATHER_MIN_KEY);
//                    Asset asset = dataMap.getAsset(WEATHER_IMAGE_KEY);
//                    Log.d("DataSent", "From Service: "+max);
//                }
//            }
//        }
//    }

//    @Override
//    public void onConnected(Bundle bundle) {
////        Wearable.DataApi.addListener(mGoogleApiClient, this);
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.d("DataSent","Connection Suspended");
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Log.d("DataSent","Connection Failed");
//    }
//    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {
//
//        @Override
//        protected Bitmap doInBackground(Asset... params) {
//
//            if (params.length > 0) {
//
//                Asset asset = params[0];
//
//                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                        mGoogleApiClient, asset).await().getInputStream();
//
//                if (assetInputStream == null) {
//                    Log.w("DataSent", "Requested an unknown Asset.");
//                    return null;
//                }
//                return BitmapFactory.decodeStream(assetInputStream);
//
//            } else {
//                Log.e("DataSent", "Asset must be non-null");
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//
//            if (bitmap != null) {
////                LOGD(TAG, "Setting background image on second page..");
////                moveToPage(1);
////                mAssetFragment.setBackgroundImage(bitmap);
//            }
//        }
//    }
}
