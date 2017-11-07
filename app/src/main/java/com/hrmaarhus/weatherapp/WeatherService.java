package com.hrmaarhus.weatherapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hrmaarhus.weatherapp.model.CityWeather;
import com.hrmaarhus.weatherapp.utils.NotificationHelper;
import com.hrmaarhus.weatherapp.utils.WeatherParser;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.hrmaarhus.weatherapp.utils.Globals.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Globals.DB_LIST_KEY;
import static com.hrmaarhus.weatherapp.utils.Globals.LOG_TAG;
import static com.hrmaarhus.weatherapp.utils.Globals.NEW_WEATHER_EVENT;
import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_ID;
import static com.hrmaarhus.weatherapp.utils.Globals.NOTIFICATION_CHANNEL_NAME;
import static com.hrmaarhus.weatherapp.utils.Globals.ONE_CITY_WEATHER_EXTRA;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CHECK_DELAY;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CITY_EVENT;

public class WeatherService extends IntentService {
    //creating a binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<String> _cityList;
    NotificationHelper notificationHelper;

    private String API_KEY = "b53c8005699265cde5eec630288d21dc";
    private String URL = "http://api.openweathermap.org/data/2.5/weather?";

    //api key b53c8005699265cde5eec630288d21dc
    //example url http://api.openweathermap.org/data/2.5/weather?q=Aarhus,dk&appid=b53c8005699265cde5eec630288d21dc

    private HashMap<String, CityWeatherData> citiesWeatherMap;

    private int numberOfRequestsToMake = 0;

    public WeatherService(){
        super("WeatherService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //creating local list of city names
        //and a map of weather data for these cities, stored as <city string, city weather> pairs
        _cityList = new ArrayList<String>();

        citiesWeatherMap = new HashMap<String, CityWeatherData>();

        _cityList = getCityListFromDb();
        //now _cityList contains a list of city strings (eg. 'Aarhus,dk')

        //most service functions below use citiesWeatherMap,
        //so create the map with city strings as keys
        createCitiesWeatherMap(_cityList, citiesWeatherMap);
        Log.d(LOG_TAG, "WeatherService _citiesList contains "+_cityList.size() + " cities after on create");

    }

    //creates cities weather map with keys from @param citiesWeatherList
    //and empty CityWeatherData object as second elem of pair
    private void createCitiesWeatherMap(ArrayList<String> citiesWeatherList,
                                        HashMap<String, CityWeatherData> citiesWeatherMap){
        if(citiesWeatherList != null){
            for(String cityString : citiesWeatherList){
                citiesWeatherMap.put(cityString, new CityWeatherData());

            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"WeatherService: onstartcommand");
        //when service is started, start checking weather periodically
        setUpWeatherChecker();

        return super.onStartCommand(intent, flags, startId);
    }

    //prepares and starts checker of the weather
    // weather will be checked every WEATHER_CHECK_DELAY milliseconds
    // weather will be checked for cities in citiesWeatherMap
    //todo cities list check!
    private void setUpWeatherChecker(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run(){
                //updates weather in locally stored map of all cities and weather in them
                updateWeatherCitiesMap(citiesWeatherMap);

                handler.postDelayed(this, WEATHER_CHECK_DELAY);
                Log.d(LOG_TAG,"---- "+ WEATHER_CHECK_DELAY + " milisecs gone, bg runner");

            }
        }, 0);
    }

    //executed when an intent is received from a client
    //which is binding to this service
    @Override
    protected void onHandleIntent(Intent intent) {
    }

    //---------------------------------------------------------------database management
    //add new city to the local citiesWeatherMap
    //sending weather data request for that city
    public void addCity(String cityString){
        Log.d(LOG_TAG,"WeatherService addCity() adding "+ cityString + " to city list");

        citiesWeatherMap.put(cityString, new CityWeatherData());

        //saving new list to a database
        saveCityListToDb();

        //request weather check for one city
        numberOfRequestsToMake = 1;
        handleOneCityWeatherData(cityString, citiesWeatherMap);

    }

    //Remove city from cities map
    public void removeCity(String cityString){
        if(citiesWeatherMap.containsKey(cityString)){
            citiesWeatherMap.remove(cityString);

            //saving changed list to db
            saveCityListToDb();
        }
    }
    //todo remove, for testing only
    public void clearDb(){
        citiesWeatherMap.clear();
        saveCityListToDb();
    }

    //save citiesWeatherMap keys (city strings) to Db.
    //The list is formated to a jsonstring and saved as a string.
    private void saveCityListToDb(){
        Log.d(LOG_TAG,"WeatherService saveCityListToDb saving list with " + citiesWeatherMap.size() + " cities");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String cityListAsJson = gson.toJson(citiesWeatherMap.keySet());

        editor.putString(DB_LIST_KEY,cityListAsJson);
        editor.apply();

    }

    //returns the list of cities from the db if it exists- empty list otherwise
    private ArrayList<String> getCityListFromDb(){
        ArrayList<String> citiesList = new ArrayList<String>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.contains(DB_LIST_KEY)){
            String jsonCityList = sharedPreferences.getString(DB_LIST_KEY, "");
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            citiesList = gson.fromJson(jsonCityList, type);
        }else{
            citiesList = new ArrayList<String>();
        }
        Log.d(LOG_TAG,"WeatherService GetCityListFromDb getting list with " + citiesList.size() + " cities");
        return citiesList;
    }


    //----------------------------------------------manage weather updates for local list of cities

    //updates CityWeatherData object whose city string is @param cityString
    //in @param cityWeatherDataMap
    //with @param newCityWeatherData, that contains updated weather data
    private void updateOneCityWeatherData(String cityString, CityWeatherData newCityWeatherData,
                                          HashMap<String, CityWeatherData> cityWeatherDataMap){
        if(cityWeatherDataMap.containsKey(cityString)){
            //todo could add identity check newCityWeatherData!=prevCityWeatherData
            cityWeatherDataMap.remove(cityString);
            cityWeatherDataMap.put(newCityWeatherData.getCityName(), newCityWeatherData);
        }
    }

    //sends http request with volley to the weather api
    //updates weather of one CityWeatherData object in the @param cityWeatherDataMap
    //that object is the one whose city string (eg. 'Aarhus,dk') is @param cityString
    //used : https://stackoverflow.com/questions/44958795/android-volley-get-callback-when-all-request-finished
    public void handleOneCityWeatherData(final String cityString, final HashMap<String, CityWeatherData> cityWeatherDataMap){
        String cityUrl = URL + "q=" + cityString + "&appid=" + API_KEY;
        Log.d(LOG_TAG,"sending weather request to: " + cityUrl);

        //instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        //request a string response from the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                cityUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //create cityWeatherData object as a result from parsing
                //data received from weather api
                CityWeatherData cityWeatherData = WeatherParser.parseCityWeatherJsonWithGson(response);
                cityWeatherData.setTimestamp(Calendar.getInstance().getTime());


                if(cityWeatherData!=null){
                    //update that weather data in locally stored city weather data map
                    updateOneCityWeatherData(cityString, cityWeatherData, cityWeatherDataMap);

                }else{
                    //problem with parsing gson, set that city weather as an empty CityWeatherDataObject
                    cityWeatherData = new CityWeatherData();
                    updateOneCityWeatherData(cityString, cityWeatherData, cityWeatherDataMap);
                    Log.d(LOG_TAG, "problem with parsing gson (api problem maybe?)");
                }

                numberOfRequestsToMake--;
                if (numberOfRequestsToMake == 0) {
                    //all requests finished, so all cities on the map are updated
                    //send broadcast to listeners about data being ready
                    notifyOnWeatherUpdate();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG,"err : "+error.getLocalizedMessage());
                Toast.makeText(WeatherService.this,
                        "error while getting weather", Toast.LENGTH_SHORT).show();

                CityWeatherData cityWeatherData = new CityWeatherData();
                //if there was a problem with getting weather for one city, set it as an empty
                //CityWeatherData object (so that the user can see which city caused the problem
                updateOneCityWeatherData(cityString, cityWeatherData, cityWeatherDataMap);

                //broadcast about new weather data available will not be called,
                //unless all requests are made (that is numberOfRequestsToMake reaches 0)
                //if one request failed, we shouldn't block the update
                numberOfRequestsToMake--;
                if(numberOfRequestsToMake == 0) {
                    notifyOnWeatherUpdate();
                }

            }
        });

        //add request to the RequestQueue
        queue.add(stringRequest);
    }

    //updates weather data for all cities in @param cityWeatherDataMap
    private void updateWeatherCitiesMap(HashMap<String, CityWeatherData> cityWeatherDataMap){
        // a broadcast should be sent
        // after updating weather for all cities locally (in the map),
        // however calls to the api are async
        // so we set numberOfRequestsToMake variable
        // to the number of cities in the map
        // after response is received from the api, this var is decremented
        // when variable reaches 0, weather for all cities in the map has been updated
        //todo could be done with an observer pattern (calling notifyOn... function)
        numberOfRequestsToMake = cityWeatherDataMap.size();

        String cityString;
        if(cityWeatherDataMap != null){
            for (HashMap.Entry<String, CityWeatherData> city : cityWeatherDataMap.entrySet()){
                cityString = city.getKey();
                //performs api call for each city
                handleOneCityWeatherData(cityString, cityWeatherDataMap);

            }
        }
    }

    //-------------------------------------------handling binding
    public class LocalBinder extends Binder {
        WeatherService getService(){
            //return this instance of WeatherService, so that clients can call its public methods
            return WeatherService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "WeatherService sb bound to the service");
        return mBinder;
    }


    //notifies 'listeners' on new weather data availability
    //in a local broadcast and a notification
    private void notifyOnWeatherUpdate(){
        Log.d(LOG_TAG,"WeatherService: notifyOnWeatherUpdate() sending weather update broadcast AND a notification");
        //sets a notification
        //todo commented this out
        Notify();
        //sends the broadcast
        Intent updateIntent = new Intent(NEW_WEATHER_EVENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }


    //-----------------------------------functions available to clients, used for getting weather data
    //returns CityWeatherData objects from citiesWeatherMap object, as a list
    public List<CityWeatherData> getAllCitiesWeather(){
        ArrayList<CityWeatherData> cityWeatherDataArrayList =
                new ArrayList<CityWeatherData>(citiesWeatherMap.values());

        return cityWeatherDataArrayList;
    }

    //returns CityWeatherData object from citiesWeatherMap if it exists
    //empty object otherwise
    public CityWeatherData getCurrentWeather(String cityName){
        CityWeatherData cityWeatherData;
        if(citiesWeatherMap.containsKey(cityName)){
            cityWeatherData = citiesWeatherMap.get(cityName);
        }else{
            cityWeatherData = new CityWeatherData();
        }

        return cityWeatherData;
    }


    //method clients can call to get weather data update on all cities
    public void requestAllCitiesWeatherUpdate(){
        updateWeatherCitiesMap(citiesWeatherMap);
    }

    private void Notify(){
        notificationHelper = new NotificationHelper(this);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentdate = sdf.format(date);
        notificationHelper.CreateNotification(getResources()
                .getString(R.string.app_name),  "Last checked weather at: " + currentdate);
    }
}
