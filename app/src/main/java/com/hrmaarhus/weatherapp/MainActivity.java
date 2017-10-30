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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static com.hrmaarhus.weatherapp.utils.Common.CELSIUS_UNICODE;
import static com.hrmaarhus.weatherapp.utils.Common.CITY_NAME_EXTRA;
import static com.hrmaarhus.weatherapp.utils.Common.CITY_WEATHER_DATA;
import static com.hrmaarhus.weatherapp.utils.Common.WEATHER_CITY_EVENT;

public class MainActivity extends AppCompatActivity {
    Button refreshBtn;
    TextView cityNameTextView, tempTextView, humidityTextView, descriptionTextView;

    WeatherService mWeatherService;
    boolean mBound = false;
    String cityName = "Aarhus";
    private String _cityName;
    private Button _addCityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshBtn = (Button)findViewById(R.id.refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWeatherService.getCurrentWeather(cityName);
            }
        });

        _addCityBtn = findViewById(R.id.addCityBtn);
        _addCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        cityNameTextView = (TextView)findViewById(R.id.cityNameTextView);
        tempTextView = (TextView)findViewById(R.id.tempTextView);
        humidityTextView = (TextView)findViewById(R.id.humidityTextView);
        descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);

        //todo both needed, but where?
        //starting the weather service
        Intent weatherIntent = new Intent(getApplicationContext(), WeatherService.class);
        startService(weatherIntent);

        //binding to WeatherService
        weatherIntent.putExtra(CITY_NAME_EXTRA,"Aarhus");
        bindService(weatherIntent, mConnection, Context.BIND_AUTO_CREATE);


        //creating local broadcast receiver
        //to get data from weather service
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mWeatherReceiver,
                        new IntentFilter(WEATHER_CITY_EVENT));

        cityNameTextView.setText("waiting for service");
        Log.d("MR","city name = " + cityName);

    }


    public void displayCityWeatherData(CityWeatherData cityWeatherData){
        cityNameTextView.setText("city: " + cityWeatherData.cityName);
        humidityTextView.setText("humidity: " + cityWeatherData.humidity.toString());
        tempTextView.setText("temperature: " + cityWeatherData.temperature.toString() + CELSIUS_UNICODE);
        descriptionTextView.setText("description: " + cityWeatherData.weatherDescription);
    }


    //-----------broadcast receiver
    private BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //received local broadcast from weather service
            //updating ui
            Toast.makeText(context, "received broadcast!", Toast.LENGTH_SHORT).show();

            CityWeatherData cityWeatherData = (CityWeatherData)intent
                    .getSerializableExtra(CITY_WEATHER_DATA);

            displayCityWeatherData(cityWeatherData);
        }
    };


    //-----------handling connection with service
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

}
