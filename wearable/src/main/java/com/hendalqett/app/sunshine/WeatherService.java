package com.hendalqett.app.sunshine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.otto.Bus;

import java.io.InputStream;

import modules.Weather;
import observers.BusProvider;

public class WeatherService extends WearableListenerService {

    private static final String WEATHER_MAX_KEY = "weather_max";
    private static final String WEATHER_MIN_KEY = "weather_min";
    private static final String WEATHER_IMAGE_KEY = "weather_photo";
    GoogleApiClient mGoogleApiClient;
    Bus bus;
    String max,min;
    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        bus= BusProvider.getInstance();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
         for (DataEvent dataEvent : dataEvents)
         {
             if (dataEvent.getType()==DataEvent.TYPE_CHANGED)
             {
                 DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                 String path = dataEvent.getDataItem().getUri().getPath();
                 if (path.equals("/com.hend.weather"))
                 {
                      max = dataMap.getString(WEATHER_MAX_KEY);
                      min = dataMap.getString(WEATHER_MIN_KEY);
                     Asset asset = dataMap.getAsset(WEATHER_IMAGE_KEY);
                     Log.d("DataSent", "From Service: "+max);
                     new LoadBitmapAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,asset);

                 }
             }
         }
    }

    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("DataSent", "Pre Load");
        }

        @Override
        protected Bitmap doInBackground(Asset... params) {


            try {
                if (params.length > 0) {

                    Asset asset = params[0];

                    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                            mGoogleApiClient, asset).await().getInputStream();

                    if (assetInputStream == null) {
                        Log.w("DataSent", "Requested an unknown Asset.");
                        return null;
                    }
                    return BitmapFactory.decodeStream(assetInputStream);

                } else {
                    Log.e("DataSent", "Asset must be non-null");
                    return null;
                }
            }
            catch (Exception e)
            {
                Log.e("DataSent", "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

//            if (bitmap != null) {

                bus.post(new Weather(max,min,bitmap));
                Log.e("DataSent", "Posted");
//            }
        }
    }
}
