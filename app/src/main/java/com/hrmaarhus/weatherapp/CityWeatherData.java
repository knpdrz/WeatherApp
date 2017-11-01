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
        this.cityName = "";
        this.humidity = 0.0;
        this.temperature = 0.0;
        this.iconType = "";
        this.weatherDescription = "";
        this.timestamp = "";
    }

    public CityWeatherData(String cityName, Double humidity, Double temperature, String iconType, String weatherDescription, String timestamp) {
        this.cityName = cityName;
        this.humidity = humidity;
        this.temperature = temperature;
        this.iconType = iconType;
        this.weatherDescription = weatherDescription;
        this.timestamp = timestamp;
    }

    public String getCityName() {
        return cityName;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getIconType() {
        return iconType;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
