package com.elapse.easyweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by YF_lala on 2018/5/17.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public AQI aqi;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
