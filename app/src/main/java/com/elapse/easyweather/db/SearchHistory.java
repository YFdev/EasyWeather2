package com.elapse.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by YF_lala on 2018/7/16.
 * 保存搜索页面
 */

public class SearchHistory extends DataSupport {
    int id;
    String cityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
