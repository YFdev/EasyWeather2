package com.elapse.easyweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.elapse.easyweather.R;
import com.elapse.easyweather.db.City;
import com.elapse.easyweather.db.County;
import com.elapse.easyweather.db.Province;
import com.elapse.easyweather.gson.Forecast;
import com.elapse.easyweather.gson.Weather;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public LocationClient mLocationClient;

    private String currentCountry;
    private String currentCity;
//    private String cityID;
    private String currentProvince;
    private String currentCounty;

    private ScrollView weatherLayout;
    private TextView title_city,titleUpdateTime,degreeText,weatherInfoText,
            aqiText,pm25Text,comfortText,carWasgText,sportText;
    private LinearLayout forecastLayout;

    public static final String ADDRESS = "http://guolin.tech/api/china";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                currentCountry = bdLocation.getCountry();    //获取国家
                currentProvince = bdLocation.getProvince();    //获取省份
                currentCity = bdLocation.getCity();    //获取城市
                currentCounty = bdLocation.getDistrict();

            }
        });
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();
        currentProvince = "广东";    //获取省份
        currentCity = "广州";    //获取城市
        currentCounty = "番禺";
        initialize();

    }

    private void initialize() {
        HttpUtil.sendOkHttpRequest(ADDRESS, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: requestProvince");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String provinceText = response.body().string();
                boolean result = Utility.handleProvinceResponse(provinceText);
                Log.d(TAG, "onResponse: "+provinceText);
                final int provinceID = queryProvinceID(currentProvince);
                Log.d(TAG, "onResponse: "+provinceID);
                HttpUtil.sendOkHttpRequest(ADDRESS + "/" + provinceID, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "onFailure: requestCity");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String cityText = response.body().string();
                        Utility.handleCityResponse(cityText,provinceID);
                        Log.d(TAG, "onResponse: "+cityText);
                        final int cityID = queryCityID(currentCity,provinceID);
                        Log.d(TAG, "onResponse: "+cityID);
                        HttpUtil.sendOkHttpRequest(ADDRESS + "/" + provinceID + "/" + cityID, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG, "onFailure: requestCounty");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String countyText = response.body().string();
                                Log.d(TAG, "onResponse: "+countyText);
                                Utility.handleCountyResponse(countyText,cityID);
                                String weatherID = queryWeatherID(cityID,currentCounty);
                                requestWeather(weatherID);
                            }
                        });
                    }
                });
            }
        });
    }

    private int queryProvinceID(final String provinceName){
//        int provinceID = 0;
        List<Province> provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            for (Province province : provinceList){
                if (province.getProvinceName().equals(provinceName)){
                    return province.getProvinceCode();
                }
            }
        }
//        else {
//            HttpUtil.sendOkHttpRequest(ADDRESS, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "onFailure: ");
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String responseText = response.body().string();
//                    if (Utility.handleProvinceResponse(responseText)){
//                        queryProvinceID(provinceName);
//                    }
//
//                }
//            });
//
//        }
        return 0;
    }

    private int queryCityID( String cityName, int provinceID){
        int cityID = 0;
        Log.d(TAG, "queryCityID: "+cityName+"  "+provinceID);
        List<City> cityList = DataSupport.where("provinceId = ?", String.valueOf(provinceID)).find(City.class);
        if (cityList.size() > 0){
            for (City city:cityList){
                Log.d(TAG, "queryCityID: "+city.getId()+" "+city.getCityName()+" "+city.getProvinceId()+" "+cityName);
                if (city.getCityName().equals(cityName)){
                    cityID = city.getCityCode();
                }
            }
            return cityID;
        }
//        else{
//
//            HttpUtil.sendOkHttpRequest(ADDRESS+"/"+provinceID, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "onFailure: city");
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (Utility.handleCityResponse(response.body().string(),provinceID)){
//                        queryCityID(cityName,provinceID);
//                    }
//                }
//            });
//        }
        return 0;
    }

    private String queryWeatherID(int cityID,String countyName){
        List<County> countyList = DataSupport.where("cityId = ?",
                String.valueOf(cityID)).find(County.class);
        if (countyList.size()>0){
            for (County county : countyList){
                if (county.getCountyName().equals(countyName)){
                    return county.getWeatherId();
                }
            }
        }
        return null;
    }

    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (! permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
//            requestLocation(mLocationClient);
        }
    }

    private void requestWeather(String cityID) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                +cityID+"&key=1bd9697783404217b228bfd43d998b15";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather !=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(MainActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(MainActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(final Weather weather) {
//        String cityName = weather.basic.cityName;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_city.setText(currentCity);
                String updateTime = weather.basic.update.updateTime.split(" ")[1];
                String degree = weather.now.temputure+"℃";
                String weatherInfo = weather.now.more.info;
                titleUpdateTime.setText(updateTime);
                degreeText.setText(degree);
                weatherInfoText.setText(weatherInfo);
                forecastLayout.removeAllViews();
                for (Forecast forecast:weather.forecastList){
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.forecast_item,
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
                carWasgText.setText(carWash);
                sportText.setText(sport);
                weatherLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initView() {
        weatherLayout = findViewById(R.id.weather_layout);
        title_city = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWasgText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
    }

    private void requestLocation(LocationClient mLocationClient){
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"permission denied",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
//                    requestLocation(mLocationClient);
                }else {
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

//    public void otherCity(View view){
//        switch (view.getId()){
//            case R.id.other_city:
//                Intent intent = new Intent(MainActivity.this,search_Activity.class);
//                startActivity(intent);
//                break;
//                default:
//                    break;
//        }
//    }
}
