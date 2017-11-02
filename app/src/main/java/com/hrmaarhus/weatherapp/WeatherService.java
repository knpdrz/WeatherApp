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
import com.hrmaarhus.weatherapp.model.CityWeather;
import com.hrmaarhus.weatherapp.utils.WeatherParser;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hrmaarhus.weatherapp.utils.Globals.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Globals.DB_LIST_KEY;
import static com.hrmaarhus.weatherapp.utils.Globals.LOG_TAG;
import static com.hrmaarhus.weatherapp.utils.Globals.NEW_WEATHER_EVENT;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CHECK_DELAY;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CITY_EVENT;

public class WeatherService extends IntentService {
    //creating a binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private ArrayList<String> _cityList;

    private String API_KEY = "b53c8005699265cde5eec630288d21dc";
    private String URL = "http://api.openweathermap.org/data/2.5/weather?";

    //todo will be removed:
    String CITY_KEY = "Aarhus,dk";

    String aarhusUrl = URL + "q=" + CITY_KEY + "&appid=" + API_KEY;

    //api key b53c8005699265cde5eec630288d21dc
    //aarhus id 2624652
    //url http://api.openweathermap.org/data/2.5/weather?q=Aarhus,dk&appid=b53c8005699265cde5eec630288d21dc

    private HashMap<String, CityWeatherData> citiesWeatherMap;

    private int numberOfRequestsToMake = 0;
    private boolean hasRequestFailed = false;

    public WeatherService(){
        super("WeatherService");
        Log.d(LOG_TAG, "WeatherService constructor");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG,"WeatherService: oncreate");
        //creating local list of city names
        //and a map of weather data for these cities, stored as <city string, city weather> pairs
        _cityList = new ArrayList<String>();

        citiesWeatherMap = new HashMap<String, CityWeatherData>();


        //todo maybe better return that list in GetCityListFromDb() function?
        GetCityListFromDb();
        //now citiesWeatherList contains a list of city strings (eg. 'Aarhus,dk')

        //most service functions below use citiesWeatherMap,
        //so create it with city strings as keys
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
                Log.d(LOG_TAG,"--putting " + cityString + " to map (with empty CWD object)");
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
    // weather will be checked for cities in the citiesList
    //todo cities list check!
    private void setUpWeatherChecker(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //todo to be removed:
                //previously:
                //getCurrentWeather(CITY_KEY);

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
        //todo do nothing after binding?
    }

    //------------------------------------------database management
    //Add new city to the citylist
    public void AddCity(String city){
        Log.d(LOG_TAG,"WeatherService AddCity() adding "+ city + " to city list");
        /*if(_cityList !=null){
            if (_cityList.contains(city)){
                return;
            }
            _cityList.add(city);


            //todo for debugging only
            printCityList();
        }*/
        citiesWeatherMap.put(city, new CityWeatherData());

    }

    //Remove city from citylist
    public void RemoveCity(String city){
        if (_cityList.contains(city)){
            _cityList.remove(city);
        }
    }

    //Save citiesWeatherMap keys (city strings) to Db.
    //The list is formated to a jsonstring and saved as a string.
    private void SaveCityListToDb(){
        Log.d(LOG_TAG,"WeatherService SaveCityListToDb saving list with " + _cityList.size() + " cities");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String cityListAsJson = gson.toJson(citiesWeatherMap.keySet());

        editor.putString(DB_LIST_KEY,cityListAsJson);
        editor.apply();

    }

    //Gets the citylist from the db if it exists.
    //The string collected from the db, is set as a arraylist.
    private void GetCityListFromDb(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.contains(DB_LIST_KEY)){
            String jsonCityList = sharedPreferences.getString(DB_LIST_KEY, "");
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>(){}.getType();
            _cityList = gson.fromJson(jsonCityList, type);
        }else{
            _cityList = new ArrayList<String>();
        }
        Log.d(LOG_TAG,"WeatherService GetCityListFromDb getting list with " + _cityList.size() + " cities");
    }

    //todo for debugging only
    private void printCityList(){
        if(_cityList != null){
            for(String city :_cityList){
                Log.d(LOG_TAG, city);
            }
        }
    }

    //todo there is no guarantee this will be called
    @Override
    public void onDestroy() {
        super.onDestroy();
        SaveCityListToDb();
    }


    //----------------------------------------------manage weather updates for local list of cities

    //updates CityWeatherData object whose city string is @param cityString
    //in @param cityWeatherDataMap
    //with @param newCityWeatherData, that contains updated weather data
    private void updateOneCityWeatherData(String cityString, CityWeatherData newCityWeatherData,
                                          HashMap<String, CityWeatherData> cityWeatherDataMap){
        if(cityWeatherDataMap.containsKey(cityString)){
            //todo could add identity check newCityWeatherData!=prevCityWeatherData
            cityWeatherDataMap.put(cityString, newCityWeatherData);
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

                if(cityWeatherData!=null){
                    //update that weather data in locally stored city weather data map
                    updateOneCityWeatherData(cityString, cityWeatherData, cityWeatherDataMap);

                    numberOfRequestsToMake--;
                    if(numberOfRequestsToMake == 0){
                        //all requests finished, so all cities on the map are updated
                        //send broadcast to listeners about data being ready
                        notifyOnWeatherUpdate();
                    }
                }else{
                    //todo should this toast be shown?
                    Toast.makeText(WeatherService.this, "problem with parsing gson (api problem maybe?)", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG,"err : "+error.getLocalizedMessage());
                Toast.makeText(WeatherService.this,
                        "error while getting weather", Toast.LENGTH_SHORT).show();
            }
        });

        //add request to the RequestQueue
        queue.add(stringRequest);
    }

    //updates weather data for all cities in @param cityWeatherDataMap
    private void updateWeatherCitiesMap(HashMap<String, CityWeatherData> cityWeatherDataMap){
        String cityString;
        if(cityWeatherDataMap != null){
            for (HashMap.Entry<String, CityWeatherData> city : cityWeatherDataMap.entrySet()){
                cityString = city.getKey();
                handleOneCityWeatherData(cityString, cityWeatherDataMap);
            }

        }

        // a broadcast about updated weather data availability should be sent
        // after updating weather for all cities locally,
        // but as calls to the api are async, we keep track of all requests to me made
        // in numberOfRequestsToMake variable
        // when this number reaches 0, broadcast is sent
        //todo could be done with an observer pattern (calling notifyOn... function)
        numberOfRequestsToMake = cityWeatherDataMap.size();

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
        return mBinder;
    }

    //todo temporary solution to save db data when app closed
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "WeatherService sb unbound from this service");
        SaveCityListToDb();
        return super.onUnbind(intent);
    }

    //todo remove
    //sends a local broadcast with current weather data of one city
    private void sendWeatherUpdate(CityWeatherData cityWeatherData){
        //creating intent to send in a local broadcast
        Intent weatherIntent = new Intent(WEATHER_CITY_EVENT);
        weatherIntent.putExtra(CITY_WEATHER_DATA, cityWeatherData);

        LocalBroadcastManager.getInstance(this).sendBroadcast(weatherIntent);
    }

    //notifies 'listeners' on new weather data availability
    //in a local broadcast
    private void notifyOnWeatherUpdate(){
        Log.d(LOG_TAG,"WeatherService: notifyOnWeatherUpdate() sending weather update broadcast");
        Intent updateIntent = new Intent(NEW_WEATHER_EVENT);

        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }

    //-----------------------------------functions available to clients, used for getting weather data
    //returns CityWeatherData objects from citiesWeatherMap object, as a list
    public List<CityWeatherData> getAllCitiesWeather(){
        ArrayList<CityWeatherData> cityWeatherDataArrayList =
                new ArrayList<CityWeatherData>(citiesWeatherMap.values());

        Log.d(LOG_TAG, "Weather Service about to send city weather data with: ");
        for(CityWeatherData cityWeatherData : cityWeatherDataArrayList){
            Log.d(LOG_TAG,"::: "+cityWeatherData.getCityName());
        }
        return cityWeatherDataArrayList;
    }

    //todo CityWeatherData getCurrentWeather(String cityName)



}
