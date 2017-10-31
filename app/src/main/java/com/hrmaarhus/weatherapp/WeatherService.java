package com.hrmaarhus.weatherapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
import com.hrmaarhus.weatherapp.utils.WeatherParser;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.hrmaarhus.weatherapp.utils.Globals.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Globals.LOG_TAG;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CHECK_DELAY;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CITY_EVENT;

public class WeatherService extends IntentService {
    //creating a binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<String> _cityList;

    String API_KEY = "b53c8005699265cde5eec630288d21dc";
    //String CITY_ID = "2624652";
    String URL = "http://api.openweathermap.org/data/2.5/weather?";
    String CITY_KEY = "Aarhus,dk";

    String aarhusUrl = URL + "q=" + CITY_KEY + "&appid=" + API_KEY;

    //api key b53c8005699265cde5eec630288d21dc
    //aarhus id 2624652
    //url http://api.openweathermap.org/data/2.5/weather?q=Aarhus,dk&appid=b53c8005699265cde5eec630288d21dc

    public WeatherService(){
        super("WeatherService");
        Log.d("MR", "WeatherService constructor");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MR","WeatherService: oncreate");
        _cityList = new ArrayList<String>();
        GetCityListFromDb();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MR","WeatherService: onstartcommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //TODO do something about the city name
        //as after the first one we're getting empty intents
        //final String cityName = intent.getStringExtra(CITY_NAME_EXTRA);
        final String cityName = CITY_KEY;
        getCurrentWeather(cityName);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Log.d("MR","---- "+ WEATHER_CHECK_DELAY + " secs gone, bg runner");
                getCurrentWeather(cityName);
                handler.postDelayed(this, WEATHER_CHECK_DELAY);
            }
        }, WEATHER_CHECK_DELAY);
    }

    //Add new city to the citylist.
    public void AddCity(String city){
        if (_cityList.contains(city)){
            return;
        }
        _cityList.add(city);

        //todo for debugging only
        printCityList();
    }

    //Remove city from citylist
    public void RemoveCity(String city){
        if (_cityList.contains(city)){
            _cityList.remove(city);
        }
    }

    //Save citylist to Db.
    //The list is formated to a jsonstring and saved as a string.
    private void SaveCityListToDb(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String cityListAsJson = gson.toJson(_cityList);

        editor.putString("citylist",cityListAsJson);
        editor.commit();
    }

    //Gets the citylist from the db if it exists.
    //The string collected from the db, is set as a arraylist.
    private void GetCityListFromDb(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.contains("citylist")){
            String jsonCityList = sharedPreferences.getString("citylist", "");
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            _cityList = gson.fromJson(jsonCityList, type);
        }
    }

    //todo for debugging only
    private void printCityList(){
        if(_cityList != null){
            for(String city :_cityList){
                Log.d(LOG_TAG, city);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SaveCityListToDb();
    }

    //----------------handling binding
    public class LocalBinder extends Binder {
        WeatherService getService(){
            //return this instance of WeatherService, so that clients can call its public methods
            return WeatherService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //method available to clients
    //sending http request with volley to weather api
    //calling sendWeatherUpdate(parsed weather data) when expected response is received
    //otherwise creating error report Toast
    public void getCurrentWeather(final String cityString){
        String cityUrl = URL + "q=" + cityString + "&appid=" + API_KEY;
        Log.d(LOG_TAG,"sending weather request to: " + cityUrl);

        //instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        //request a string response from the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                cityUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CityWeatherData cityWeatherData = WeatherParser.parseCityWeatherJsonWithGson(response);
                if(cityWeatherData != null){
                    sendWeatherUpdate(cityWeatherData);
                }else{
                    //todo should this toast be shown?
                    Toast.makeText(WeatherService.this, "problem with parsing gson (api problem maybe?)", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MR","err : "+error.getLocalizedMessage());
                Toast.makeText(WeatherService.this,
                        "error while getting weather", Toast.LENGTH_SHORT).show();
            }
        });

        //add request to the RequestQueue
        queue.add(stringRequest);
    }

    //sends a local broadcast with current weather data of one city
    private void sendWeatherUpdate(CityWeatherData cityWeatherData){
        //creating intent to send in a local broadcast
        Intent weatherIntent = new Intent(WEATHER_CITY_EVENT);
        weatherIntent.putExtra(CITY_WEATHER_DATA, cityWeatherData);

        LocalBroadcastManager.getInstance(this).sendBroadcast(weatherIntent);
    }

    public void getAllCitiesWeather(){

    }


}
