package com.elapse.easyweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by YF_lala on 2018/5/17.
 */

public class Now {
    @SerializedName("tmp")
    public String temputure;

    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;
    }
}
