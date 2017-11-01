package com.hrmaarhus.weatherapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.hrmaarhus.weatherapp.utils.Globals.CELSIUS_UNICODE;
import static com.hrmaarhus.weatherapp.utils.Globals.CITY_NAME_EXTRA;
import static com.hrmaarhus.weatherapp.utils.Globals.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Globals.LOG_TAG;
import static com.hrmaarhus.weatherapp.utils.Globals.NEW_WEATHER_EVENT;
import static com.hrmaarhus.weatherapp.utils.Globals.WEATHER_CITY_EVENT;

public class MainActivity extends AppCompatActivity{
    Button refreshBtn;
    TextView cityNameTextView, tempTextView, humidityTextView, descriptionTextView;

    WeatherService mWeatherService;
    boolean mBound = false;
    String cityName = "Aarhus,dk";
    private String _cityName;
    private Button _addCityBtn;
    private EditText _newCity;

    CityAdapter adapter;
    ListView listView;

    ArrayList<CityWeatherData> citiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _newCity = (EditText) findViewById(R.id.newCityText);

        refreshBtn = (Button)findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mWeatherService.getCurrentWeather(cityName);
            }
        });

        _addCityBtn = findViewById(R.id.addCityBtn);
        _addCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newCity = _newCity.getText().toString();

                mWeatherService.AddCity(newCity);
            }
        });

        cityNameTextView = (TextView)findViewById(R.id.cityNameTextView);
        tempTextView = (TextView)findViewById(R.id.tempTextView);
        humidityTextView = (TextView)findViewById(R.id.humidityTextView);
        descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);

        //starting the weather service
        Intent weatherIntent = new Intent(getApplicationContext(), WeatherService.class);
        startService(weatherIntent);

        //binding to WeatherService
        weatherIntent.putExtra(CITY_NAME_EXTRA,cityName);
        bindService(weatherIntent, mConnection, Context.BIND_AUTO_CREATE);


        //creating local broadcast receiver
        //to be able to get data from the weather service
        //listen for 'new data available' broadcast
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mWeatherReceiver,
                        new IntentFilter(NEW_WEATHER_EVENT));

        //TODO will be removed after listview shows up
        //before broadcast with current weather is received, display placeholder
        cityNameTextView.setText("waiting for the service");

        //setting cities list that will be displayed in the list view
        citiesList = new ArrayList<CityWeatherData>();

        //todo dummy data to be removed
        citiesList.add(new CityWeatherData("Gdansk",12.0,44.0,"e1","sun shiiinin","12:23:12"));
        citiesList.add(new CityWeatherData("Orneta",0.0,20.0,"n1","brzydko","12:23:12"));
        citiesList.add(new CityWeatherData("Szczytno",50.0,14.0,"k1","ladnie","12:23:12"));

        //setting up the list view of cities
        prepareListView();
    }


    //todo remove
    public void displayCityWeatherData(CityWeatherData cityWeatherData){
        cityNameTextView.setText("city: " + cityWeatherData.cityName);
        humidityTextView.setText("humidity: " + cityWeatherData.humidity.toString());
        tempTextView.setText("temperature: " + cityWeatherData.temperature.toString() + CELSIUS_UNICODE);
        descriptionTextView.setText("description: " + cityWeatherData.weatherDescription);
    }

    //---------------------------------------------broadcast receiver
    private BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //received local broadcast from weather service
            Toast.makeText(context, "received broadcast!", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG,"MainActivity: received broadcast from weather service ");

            if(intent.getAction().equals(NEW_WEATHER_EVENT)){
                //broadcast informs about new weather data available
                //so get newest data
                Log.d(LOG_TAG,"MainActivity: there is new weather data available");

                ArrayList<CityWeatherData> cityWeatherDataArrayList =
                        (ArrayList<CityWeatherData>)mWeatherService.getAllCitiesWeather();
                updateCitiesWeatherListView(cityWeatherDataArrayList);
            }


            /*CityWeatherData cityWeatherData = (CityWeatherData)intent
                    .getSerializableExtra(CITY_WEATHER_DATA);

            displayCityWeatherData(cityWeatherData);*/
        }
    };


    //------------------------------------------------handling connection with service
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MR","mainActivity onserviceconnected");
            //after binding to WeatherService
            //casting the IBinder
            WeatherService.LocalBinder binder = (WeatherService.LocalBinder)iBinder;
            //getting WeatherService instance to call its public methods
            mWeatherService = binder.getService();

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    //todo this is only a quickfix to make sure list of cities is saved after app is closed
    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "MainActivity unbinding from the service");
        //unbinding from weather service
        if(mConnection != null){
            unbindService(mConnection);
        }
        super.onPause();

    }

    //---------------------------------------------------list view management
    private void prepareListView(){
        adapter = new CityAdapter(this, citiesList);
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //todo: go to details activity
                Toast.makeText(getApplicationContext(),
                        "you clicked on " + citiesList.get(position).getCityName(), Toast.LENGTH_SHORT).show();
                /*
                Intent startDemoIntent = new Intent();
                startDemoIntent.putExtra("position", position);
                String action = cityList.get(position).getDemoAction();
                int demoResultCode = cityList.get(position).getResultCode();
                if(action != null && !action.equals("")){
                    startDemoIntent.setAction(action);
                    startActivityForResult(startDemoIntent, demoResultCode);
                }*/
            }
        });
    }

    //update local citiesList with the list @param cityWeatherDataArrayList
    //and notify observers of adapter, so that list view refreshes
    private void updateCitiesWeatherListView(ArrayList<CityWeatherData> cityWeatherDataArrayList){
        //todo check for identity?
        //Log.d(LOG_TAG, "MainActivity list received in broadcast with " + cityWeatherDataArrayList.size() + " elements");
        citiesList = cityWeatherDataArrayList;

        //todo not the best practice?
        adapter.setData(citiesList);

        adapter.notifyDataSetChanged();
        listView.invalidateViews();
    }

}
