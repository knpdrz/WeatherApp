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

import static com.hrmaarhus.weatherapp.utils.Globals.CELSIUS_UNICODE;
import static com.hrmaarhus.weatherapp.utils.Globals.CITY_NAME_EXTRA;
import static com.hrmaarhus.weatherapp.utils.Globals.CITY_WEATHER_DATA;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _newCity = (EditText) findViewById(R.id.newCityText);

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
                String newCity = _newCity.getText().toString();

                mWeatherService.AddCity(newCity);
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
        weatherIntent.putExtra(CITY_NAME_EXTRA,cityName);
        bindService(weatherIntent, mConnection, Context.BIND_AUTO_CREATE);


        //creating local broadcast receiver
        //to be able to get data from the weather service
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mWeatherReceiver,
                        new IntentFilter(WEATHER_CITY_EVENT));

        //TODO will be removed after listview shows up
        //before broadcast with current weather is received, display placeholder
        cityNameTextView.setText("waiting for the service");

        //setting up the list view of cities
        prepareListView();
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
            //received local broadcast from weather service (with current weather)
            //updating ui
            Toast.makeText(context, "received weather broadcast!", Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unbinding from weather service
        if(mConnection != null){
            unbindService(mConnection);
        }
    }

    //--------list view management
    private void prepareListView(){
        final ArrayList<CityWeatherData> cityList = new ArrayList<>();
        cityList.add(new CityWeatherData("Gdansk",12.0,44.0,"e1","sun shiiinin","12:23:12"));
        cityList.add(new CityWeatherData("Orneta",0.0,20.0,"n1","brzydko","12:23:12"));
        cityList.add(new CityWeatherData("Szczytno",50.0,14.0,"k1","ladnie","12:23:12"));


        adapter = new CityAdapter(this, cityList);
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //todo: go to details activity
                Toast.makeText(getApplicationContext(),
                        "you clicked on " + cityList.get(position).getCityName(), Toast.LENGTH_SHORT).show();
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



}
