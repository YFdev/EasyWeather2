package com.elapse.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YF_lala on 2018/5/17.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public update update;

    public class update{
        @SerializedName("loc")
        public String updateTime;
    }
}
