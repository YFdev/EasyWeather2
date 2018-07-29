package com.elapse.easyweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elapse.easyweather.gson.Forecast;
import com.elapse.easyweather.gson.Weather;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by YF_lala on 2018/7/10.
 */
//
public class OtherFrag extends Fragment {
    private static final String TAG = "Other_frag";
    //主布局
    private ScrollView weatherLayout;
    private TextView title_city,titleUpdateTime,degreeText,weatherInfoText,
            aqiText,pm25Text,comfortText,carWashText,sportText;
    private LinearLayout forecastLayout;
    //下拉刷新布局
    public SwipeRefreshLayout swipeRefresh;
    //背景图片
    ImageView bingPic;
    //用于保存weatherID，下拉刷新时使用
    private String weatherId_fresh;
    private MainActivity activity;
    private String weatherId;
    //标记位，当页面数大于5时会直接调用requestWeather（），使用该标记位避免onCreateView()中调用之前数据
    private boolean isRequestExecuted = false;

//    Handler mHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            switch (msg.what){
//                case WeatherConst.GET_LOCATION:
//                    Log.d(TAG, "handleMessage: "+MainActivity.location.toString());
//                    String provinceName = MainActivity.location[0];
//                    queryProvince(provinceName);
//                    break;
//                case WeatherConst.GET_PROVINCE:
//                    String cityName = MainActivity.location[1];
//                    queryCity(cityName);
//                    break;
//                case WeatherConst.GET_CITY:
//                    String countyName = MainActivity.location[2];
//                    queryCounty(countyName);
//                    break;
//                case WeatherConst.GET_COUNTY:
//                    requestWeather(selectedCounty.getWeatherId());
//                    break;
//            }
//            return false;
//        }
//    });

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        Log.d(TAG, "onAttach: executed");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: executed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_frag,container,false);
        initView(view);
        //获取MainActivity中new fragment setArgument（）中的值
        weatherId = (String) getArguments().get("weatherId");
        Log.d(TAG, "onCreateView: executed");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //发起http请求
        if (!isRequestExecuted){
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId_fresh);
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    private void showWeatherInfo(final Weather weather) {
        final String cityName = weather.basic.cityName;
        //主线程刷新view
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_city.setText(cityName);
                String updateTime = weather.basic.update.updateTime.split(" ")[1];
                String degree = weather.now.temputure+"℃";
                String weatherInfo = weather.now.more.info;
                titleUpdateTime.setText(updateTime);
                degreeText.setText(degree);
                weatherInfoText.setText(weatherInfo);
                forecastLayout.removeAllViews();
                for (Forecast forecast:weather.forecastList){
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.forecast_item,
                            forecastLayout,false);
                    TextView dateText = view.findViewById(R.id.date_text);
                    TextView infoText = view.findViewById(R.id.info_text);
                    TextView maxText = view.findViewById(R.id.max_text);
                    TextView minText = view.findViewById(R.id.min_text);
                    dateText.setText(forecast.date);
                    infoText.setText(forecast.more.info);
                    maxText.setText(forecast.temperature.max);
                    minText.setText(forecast.temperature.min);
                    forecastLayout.addView(view);
                }
                if (weather.aqi != null){
                   aqiText.setText(weather.aqi.city.aqi);
                    pm25Text.setText(weather.aqi.city.pm25);
                }
                String comfort = "舒适度："+weather.suggestion.comfort.info;
                String carWash = "洗车指数："+weather.suggestion.carWash.info;
                String sport = "运动指数："+weather.suggestion.sport.info;
                comfortText.setText(comfort);
                carWashText.setText(carWash);
                sportText.setText(sport);
                weatherLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initView(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = view.findViewById(R.id.weather_layout);
        title_city = view.findViewById(R.id.title_city);
        titleUpdateTime = view.findViewById(R.id.title_update_time);
        degreeText = view.findViewById(R.id.degree_text);
        weatherInfoText = view.findViewById(R.id.weather_info_text);
        forecastLayout = view.findViewById(R.id.forecast_layout);
        aqiText = view.findViewById(R.id.aqi_text);
        pm25Text = view.findViewById(R.id.pm25_text);
        comfortText = view.findViewById(R.id.comfort_text);
        carWashText = view.findViewById(R.id.car_wash_text);
        sportText = view.findViewById(R.id.sport_text);
        bingPic = view.findViewById(R.id.bing_pic);
        //检查是否有本地图片缓存
        String bing_pic = MainActivity.prefs.getString("bing_pic",null);
        if (bing_pic != null){
            Glide.with(this).load(bing_pic).into(bingPic);
        }else{
            loadBingPic();
        }
        //检查是否有本地天气信息查询，prefs中存放的key值与weatherID关联
        String weatherString = MainActivity.prefs.getString("weather"+weatherId_fresh,null);
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId_fresh = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                SharedPreferences.Editor editor = MainActivity.prefs.edit();
                editor.putString("bing_pic",responseText);
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getActivity()).load(responseText).into(bingPic);
                    }
                });
            }
        });
    }

//    private void queryProvince(String provinceName){
//        List<Province> provinceList = DataSupport.findAll(Province.class);
//        if (provinceList.size() > 0){
//            for (Province p:provinceList){
//                if (p.getProvinceName().equals(provinceName)){
//                    selectedProvince = p;
//                    Message msg1 = new Message();
//                    msg1.what = WeatherConst.GET_PROVINCE;
//                    mHandler.sendMessage(msg1);
//                    break;
//                }
//            }
//        }else {
//            String address = "http://guolin.tech/api/china";
//            queryFromServer(address,"province",provinceName);
//        }
//    }
//
//    private void queryCity(String cityName){
//        List<City> cityList = DataSupport.where("provinceid = ?",
//                String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
//        if (cityList.size() > 0){
//            for (City c : cityList){
//                if (c.getCityName().equals(cityName)){
//                    selectedCity = c;
//                    Message msg2 = new Message();
//                    msg2.what = WeatherConst.GET_CITY;
//                    mHandler.sendMessage(msg2);
//                    break;
//                }
//            }
//        }else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            String address = "http://guolin.tech/api/china/"+provinceCode;
//            queryFromServer(address,"city",cityName);
//        }
//    }
//
//    private void queryCounty(String countyName){
//        List<County> countyList = DataSupport.where("cityid=?",String.valueOf(selectedCity.getCityCode())).find(County.class);
//        if (countyList.size() > 0){
//            for (County c : countyList){
//                if (c.getCountyName().equals(countyName)){
//                    selectedCounty = c;
//                    weatherId_fresh = c.getWeatherId();
//                    Message msg3 = new Message();
//                    msg3.what = WeatherConst.GET_COUNTY;
//                    mHandler.sendMessage(msg3);
//                    break;
//                }
//            }
//        }else {
//            int provinceCode = selectedProvince.getProvinceCode();
//            int cityCode = selectedCity.getCityCode();
//            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
//            queryFromServer(address,"county",countyName);
//        }
//    }
//
//    private void queryFromServer(String address, final String type,final String name) {
////        showProgressDialog();
//        HttpUtil.sendOkHttpRequest(address, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseText = response.body().string();
//                boolean result ;
//                if ("province".equals(type)){
//                    result = Utility.handleProvinceResponse(responseText);
//                }else if ("city".equals(type)){
//                    result = Utility.handleCityResponse(responseText,selectedProvince.getProvinceCode());
//                }else {
//                    result = Utility.handleCountyResponse(responseText,selectedCity.getCityCode());
//                }
//                if (result){
//                    if ("province".equals(type)){
//                        queryProvince(name);
//                    }else if ("city".equals(type)){
//                        queryCity(name);
//                    }else if ("county".equals(type)){
//                        queryCounty(name);
//                    }
//                }
//            }
//        });
//    }

    public void requestWeather(final String weatherId) {
        isRequestExecuted = true;
        Log.d(TAG, "requestWeather: executed");
        weatherId_fresh = weatherId;
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=1bd9697783404217b228bfd43d998b15";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: requestWeather");
               activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
//                Log.d(TAG, "onResponse: "+responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = MainActivity.prefs.edit();
                            editor.putString("weather"+weatherId,responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(getContext(),
                                    "requestWeather failed",Toast.LENGTH_SHORT).show();
                        }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }
}
