package com.elapse.easyweather.utils;

/**
 * Created by YF_lala on 2018/7/10.
 * 提供部分常量
 */

public class WeatherConst {
    //定位成功标志位
    public static final int GET_LOCATION = 0;
    //获取省成功标志位
    public static final int GET_PROVINCE = 1;
    //获取省会城市成功标志位
    public static final int GET_CITY = 2;
    //获取市成功标志位
    public static final int GET_COUNTY = 3;
    //记录当前listitem出现“取消 删除”的index
    public static  int currentIndex = -1;
    //记录上一个listitem出现“取消 删除”的index
    public static  int oldIndex = -1;
}
