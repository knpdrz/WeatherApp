package com.hrmaarhus.weatherapp.model;

/**
 * Created by kasper on 30/04/16.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CityWeather {

    @SerializedName("coord")
    @Expose
    public Coord coord;
    @SerializedName("weather")
    @Expose
    public List<Weather> weather = null;
    @SerializedName("base")
    @Expose
    public String base;
    @SerializedName("main")
    @Expose
    public Main main;
    @SerializedName("wind")
    @Expose
    public Wind wind;
    @SerializedName("clouds")
    @Expose
    public Clouds clouds;
    @SerializedName("dt")
    @Expose
    public Integer dt;
    @SerializedName("sys")
    @Expose
    public Sys sys;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("cod")
    @Expose
    public Integer cod;

    @Override
    public String toString() {
        return "CityWeather{name = " +
                name + "\n"
                + main.humidity+ "\n"
                + main.temp + "\n"
                + weather.get(0).icon+ "\n"
                + weather.get(0).description+ "\n"

                +'}';
    }
}
//city name
//humidity
//temp
//icontype
//weather description
//timestamp
