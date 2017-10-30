package com.hrmaarhus.weatherapp;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hrmaarhus.weatherapp.utils.WeatherParser;

import static com.hrmaarhus.weatherapp.utils.Common.CITY_NAME_EXTRA;
import static com.hrmaarhus.weatherapp.utils.Common.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Common.WEATHER_CHECK_DELAY;
import static com.hrmaarhus.weatherapp.utils.Common.WEATHER_CITY_EVENT;

public class WeatherService extends IntentService {
    //creating a binder given to clients
    private final IBinder mBinder = new LocalBinder();

    String API_KEY = "b53c8005699265cde5eec630288d21dc";
    String CITY_ID = "2624652";
    String URL = "http://api.openweathermap.org/data/2.5/weather?";

    String aarhusUrl = URL + "id=" + CITY_ID + "&appid=" + API_KEY;

    //api key b53c8005699265cde5eec630288d21dc
    //aarhus id 2624652
    //url http://api.openweathermap.org/data/2.5/weather?id=2624652&appid=b53c8005699265cde5eec630288d21dc

    public WeatherService(){
        super("WeatherService");
        Log.d("MR", "WeatherService constructor");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MR","WeatherService: oncreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MR","WeatherService: onstartcommand");
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        final String cityName = intent.getStringExtra(CITY_NAME_EXTRA);
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
    public void getCurrentWeather(final String cityName){
        //todo add differentiation between cities
        String cityUrl = aarhusUrl;

        //instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        //request a string response from the url
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                cityUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                CityWeatherData cw;

                CityWeatherData cityWeatherData = WeatherParser.parseCityWeatherJsonWithGson(response);
                if(cityWeatherData != null){
                    sendWeatherUpdate(cityWeatherData);
                }else{
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
    
    private void sendWeatherUpdate(CityWeatherData cityWeatherData){
        //Log.d("MR","WeatherService sendWeatherUpdate " + weatherString);
        //creating intent to send in a local broadcast
        Intent weatherIntent = new Intent(WEATHER_CITY_EVENT);
        weatherIntent.putExtra(CITY_WEATHER_DATA, cityWeatherData);

        LocalBroadcastManager.getInstance(this).sendBroadcast(weatherIntent);
    }
}
