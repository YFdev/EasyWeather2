package com.elapse.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by YF_lala on 2018/7/16.
 */

public class PagerList extends DataSupport {
    int id;
    int pageNum;
    String weatherId;
    String cityName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
