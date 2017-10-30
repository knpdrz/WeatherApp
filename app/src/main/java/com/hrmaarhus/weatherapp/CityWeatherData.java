package com.hrmaarhus.weatherapp;


import java.io.Serializable;

/**
 * Created by Moon on 29.10.2017.
 */

public class CityWeatherData implements Serializable {
    public String cityName;
    public Double humidity;
    public Double temperature;
    public String iconType;
    public String weatherDescription;
    public String timestamp;

    public CityWeatherData(){

    }

    public CityWeatherData(String cityName, Double humidity, Double temperature, String iconType, String weatherDescription, String timestamp) {
        this.cityName = cityName;
        this.humidity = humidity;
        this.temperature = temperature;
        this.iconType = iconType;
        this.weatherDescription = weatherDescription;
        this.timestamp = timestamp;
    }
}
