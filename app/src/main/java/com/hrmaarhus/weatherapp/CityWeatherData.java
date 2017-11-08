package com.hrmaarhus.weatherapp;


import java.io.Serializable;
import java.util.Date;

/**
 * Created by Moon on 29.10.2017.
 */

public class CityWeatherData implements Serializable {
    private String cityName;
    private Double humidity;
    private Double temperature;
    private String iconType;
    private String weatherDescription;
    private Date timestamp;

    public CityWeatherData(){
        this.cityName = "";
        this.humidity = 0.0;
        this.temperature = 0.0;
        this.iconType = "";
        this.weatherDescription = "";
        this.timestamp = new Date();
    }

    public CityWeatherData(String cityName, Double humidity, Double temperature, String iconType, String weatherDescription, Date timestamp) {
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp;}
}
