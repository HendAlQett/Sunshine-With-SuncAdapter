package com.hendalqett.app.sunshine;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hendalqett.app.sunshine.data.WeatherContract;
import com.hendalqett.app.sunshine.service.SunshineService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    View rootView;


    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    final static int FORECAST_LOADER = 0;

    private ForecastAdapter mForecastAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private boolean mUseTodayLayout;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//                locationSetting, System.currentTimeMillis());

//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);


        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.

        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
//        ArrayList<String> weatherForecastList = new ArrayList<>();
//        weatherForecastList.add("Today - Sunny - 88/63");
//        weatherForecastList.add("Tomorrow - Foggy - 70/46");
//        weatherForecastList.add("Weds - Cloudy - 72/63");
//        weatherForecastList.add("Thurs - Rainy - 64/51");
//        weatherForecastList.add("Fri - Foggy - 70/46");
//        weatherForecastList.add("Sat - Sunny - 76/68");


        //Arrays.asList(array)
//        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forcast_textview, weatherForecastList);


//        list.setAdapter(mForecastAdapter);
//
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object item = parent.getItemAtPosition(position);
////                Toast.makeText(getActivity(), item.toString(),Toast.LENGTH_SHORT ).show();
//                //I can use adapter instead of parent, both work
//                String forecast = parent.getItemAtPosition(position).toString();
//                Toast.makeText(getActivity(), forecast.toString(), Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT, forecast);
//                startActivity(intent);
//            }
//        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());

                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));

                    mPosition = position;
                }
            }
        });

//        getActivity().getSupportLoaderManager().initLoader(0, null, this);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {

            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateWeather() {

//        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        String location = Utility.getPreferredLocation(getActivity());
        //weatherTask.execute(location);

        //1- create an intent for the receiver
        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,Utility.getPreferredLocation(getActivity()));
        //2- Wrap this intent in a pending intent.
        PendingIntent pendingIntent= PendingIntent.getBroadcast(getActivity(),0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        //3- Get the Alarm service
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        //4- Set the Alarm to trigger 5 seconds from now
        am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+5000,pendingIntent);
        //5- Start the service inside onReceive
//        Intent intent = new Intent(getActivity(), SunshineService.class);
//        intent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
//              Utility.getPreferredLocation(getActivity()));
//        getActivity().startService(intent);
    }

//    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private String formatHighLows(double high, double low, String unitType) {
//
//            if (unitType.equals(getString(R.string.pref_units_imperial))) {
//                high = (high * 1.8) + 32;
//                low = (low * 1.8) + 32;
//            } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
//                Log.d(LOG_TAG, "Unit type not found: " + unitType);
//            }
//
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//
//            String[] resultStrs = new String[numDays];
//
//            // Data is fetched in Celsius by default.
//            // If user prefers to see in Fahrenheit, convert the values here.
//            // We do this rather than fetching in Fahrenheit so that the user can
//            // change this option without us having to re-fetch the data once
//            // we start storing the values in a database.
//            SharedPreferences sharedPrefs =
//                    PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String unitType = sharedPrefs.getString(
//                    getString(R.string.pref_units_key),
//                    getString(R.string.pref_units_metric));
//
//            for (int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay + i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low, unitType);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//            return resultStrs;
//
//        }
//
//
//        @Override
//        protected String[] doInBackground(String... params) {
//
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are avaiable at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//
//                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily"; //I can add or remove the question mark and it is going to work anyway
//                Uri uriBuilder = Uri.parse(FORECAST_BASE_URL).buildUpon().
//
//                        appendQueryParameter("q", params[0]).
//                        appendQueryParameter("mode", format).
//                        appendQueryParameter("units", units).
//                        appendQueryParameter("cnt", Integer.toString(numDays)).build();
//                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
//                URL url = new URL(uriBuilder.toString());
//
//                Log.d(LOG_TAG, url.toString());
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);
//                return getWeatherDataFromJson(forecastJsonStr, numDays);
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attempting
//                // to parse it.
//                return null;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            return null;
//        }
//
//        //Ctrl+ O to show methods to implement
//        @Override
//        protected void onPostExecute(String[] strings) {
//            super.onPostExecute(strings);
//            if (strings != null) {
////            adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forcast_textview, Arrays.asList(strings));
//                //OR
//                adapter.clear();
//                //I can use addAll method starting HoneyComb
//                for (String dayForeCast : strings) {
//                    adapter.add(dayForeCast);
//                }
//
//                list.setAdapter(adapter);
//            }
//        }
//
//        //        @Override
////        protected void onPostExecute(String[] strings) {
////            super.onPostExecute(strings);
////            Log.d(LOG_TAG,strings[0]);
////
////
////        }
//    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
          * so for convenience we're breaking it out into its own method now.
          */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

//    private String formatHighLows(double high, double low, String unitType) {
//
//        if (unitType.equals(getString(R.string.pref_units_imperial))) {
//            high = (high * 1.8) + 32;
//            low = (low * 1.8) + 32;
//        } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
//            Log.d(LOG_TAG, "Unit type not found: " + unitType);
//        }
//
//        // For presentation, assume the user doesn't care about tenths of a degree.
//        long roundedHigh = Math.round(high);
//        long roundedLow = Math.round(low);
//
//        String highLowStr = roundedHigh + "/" + roundedLow;
//        return highLowStr;
//    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
//    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//            throws JSONException {
//
//        // These are the names of the JSON objects that need to be extracted.
//        final String OWM_LIST = "list";
//        final String OWM_WEATHER = "weather";
//        final String OWM_TEMPERATURE = "temp";
//        final String OWM_MAX = "max";
//        final String OWM_MIN = "min";
//        final String OWM_DESCRIPTION = "main";
//
//        JSONObject forecastJson = new JSONObject(forecastJsonStr);
//        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//        // OWM returns daily forecasts based upon the local time of the city that is being
//        // asked for, which means that we need to know the GMT offset to translate this data
//        // properly.
//
//        // Since this data is also sent in-order and the first day is always the
//        // current day, we're going to take advantage of that to get a nice
//        // normalized UTC date for all of our weather.
//
//        Time dayTime = new Time();
//        dayTime.setToNow();
//
//        // we start at the day returned by local time. Otherwise this is a mess.
//        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//        // now we work exclusively in UTC
//        dayTime = new Time();
//
//        String[] resultStrs = new String[numDays];
//        for (int i = 0; i < weatherArray.length(); i++) {
//            // For now, using the format "Day, description, hi/low"
//            String day;
//            String description;
//            String highAndLow;
//
//            // Get the JSON object representing the day
//            JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//            // The date/time is returned as a long.  We need to convert that
//            // into something human-readable, since most people won't read "1400356800" as
//            // "this saturday".
//            long dateTime;
//            // Cheating to convert this to UTC time, which is what we want anyhow
//            dateTime = dayTime.setJulianDay(julianStartDay + i);
//            day = getReadableDateString(dateTime);
//
//            // description is in a child array called "weather", which is 1 element long.
//            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//            description = weatherObject.getString(OWM_DESCRIPTION);
//
//            // Temperatures are in a child object called "temp".  Try not to name variables
//            // "temp" when working with temperature.  It confuses everybody.
//            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//            double high = temperatureObject.getDouble(OWM_MAX);
//            double low = temperatureObject.getDouble(OWM_MIN);
//
//            highAndLow = formatHighLows(high, low);
//            resultStrs[i] = day + " - " + description + " - " + highAndLow;
//        }
//
//        for (String s : resultStrs) {
//            Log.v(LOG_TAG, "Forecast entry: " + s);
//        }
//        return resultStrs;
//
//    }
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        // TODO: add parsing code here
        double max = 0;
        String jsonString = "";
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(weatherJsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            //ArrayList maximumList = new ArrayList<>();
            //for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tempObject = jsonArray.getJSONObject(dayIndex).getJSONObject("temp");
            double min = tempObject.getDouble("min");
            max = tempObject.getDouble("max");
            //maximumList.add(max);

            JSONArray weatherArray = jsonArray.getJSONObject(dayIndex).getJSONArray("weather");
            String main = weatherArray.getJSONObject(0).getString("main");
            // }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return max;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.
        String locationSettings = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSettings, System.currentTimeMillis());

        Log.d(LOG_TAG, weatherForLocationUri.toString());

        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);

    }

    public void setUseTodayLayout(boolean useTodayLayout) {
               mUseTodayLayout = useTodayLayout;
               if (mForecastAdapter != null) {
                       mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
                   }
         }
}

