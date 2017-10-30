package com.hrmaarhus.weatherapp.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hrmaarhus.weatherapp.CityWeatherData;
import com.hrmaarhus.weatherapp.model.CityWeather;


/**
 * Created by Moon on 29.10.2017.
 */

public class WeatherParser {
    private static final double TO_CELSIUS_FROM_KELVIN = -273.15;

    //parsing with Gson - note that the Gson parser uses the model object CityWeather,
    // Clouds, Coord, Main, Sys, Weather and Wind extracted with http://www.jsonschema2pojo.org/
    public static CityWeatherData parseCityWeatherJsonWithGson(String jsonString){
        Gson gson = new GsonBuilder().create();
        CityWeather weatherInfo =  gson.fromJson(jsonString, CityWeather.class);
        if(weatherInfo != null) {
            Log.d("MR",weatherInfo.toString());

            CityWeatherData cityWeatherData = new CityWeatherData(
                    weatherInfo.name,
                    weatherInfo.main.humidity,
                    weatherInfo.main.temp.doubleValue() + TO_CELSIUS_FROM_KELVIN,
                    weatherInfo.weather.get(0).icon,
                    weatherInfo.weather.get(0).description,
                    "timestamp");


            return cityWeatherData;
        } else {
            Log.d("MR","WeatherParser: could not parse from gson!");
            return null;
        }
    }
}
