package com.elapse.easyweather;

import android.Manifest;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.baidu.location.LocationClientOption;
import com.elapse.easyweather.db.City;
import com.elapse.easyweather.db.County;
import com.elapse.easyweather.db.Province;
import com.elapse.easyweather.gson.Forecast;
import com.elapse.easyweather.gson.Weather;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";

    public LocationClient mLocationClient;

    private ScrollView weatherLayout;
    private TextView title_city,titleUpdateTime,degreeText,weatherInfoText,
            aqiText,pm25Text,comfortText,carWashText,sportText;
    private LinearLayout forecastLayout;

    Handler mHandler;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new mBDAbstractLocationListener());
        setContentView(R.layout.activity_main);
        requestPermission();
        initView();

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
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        prefs = PreferenceManager.getDefaultSharedPreferences(Main2Activity.this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            mHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    Bundle b = msg.getData();
        //                    String weatherId = (String) b.get("weather_id");
        //                    requestWeather(weatherId);
                    String provinceName = b.getString("cur_province","广东");
                    String cityName = b.getString("cur_city","深圳");
                    String countyName = b.getString("cur_county","深圳");
//                    title_city.setText(countyName);
                    Log.d(TAG, "handleMessage: "+provinceName+" "+cityName+" "+countyName);
                    queryWeather(provinceName,cityName,countyName);
                    Log.d(TAG, "handleMessage: query weather done");
                    return true;
                }
            });
        }
    }

   public void queryWeather(final String provinceName, final String cityName, final String countyName){
        final int provinceId = queryProvinceIdFromLocal(provinceName);
        Log.d(TAG, "queryWeather: start query"+provinceId);
        if (provinceId != -1){
            //check if cityId is invalid
            final int cityId = queryCityIdFromLocal(provinceId,cityName);
            if (cityId != -1){
                //check if county is null
                String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                if (!TextUtils.isEmpty(weatherId)){
                    //requestWeather
                    requestWeather(weatherId);
                }else{
                    //query weather info from server
                    HttpUtil.sendOkHttpRequest(address + "/" + provinceId + "/" + cityId,
                            new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "onFailure: request county info");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String countyText = response.body().string();
                            Utility.handleCountyResponse(countyText,cityId);
                            String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                            if (!TextUtils.isEmpty(weatherId)){
                                //requestWeather
                                requestWeather(weatherId);
                             }
                        }
                    });
                }
            }else {
                //query city info from server
                Log.d(TAG, "queryWeather: query city info from server");
                HttpUtil.sendOkHttpRequest(address + "/" + provinceId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d(TAG, "onFailure: query city info");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String cityText = response.body().string();
                        Utility.handleCityResponse(cityText,provinceId);
                        String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                        if (!TextUtils.isEmpty(weatherId)){
                            //requestWeather
                            requestWeather(weatherId);
                        }else{
                            //query weather info from server
                            HttpUtil.sendOkHttpRequest(address + "/" + provinceId + "/" + cityId, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.d(TAG, "onFailure: request county info");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String countyText = response.body().string();
                                    Utility.handleCountyResponse(countyText,cityId);
                                    String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                                    if (!TextUtils.isEmpty(weatherId)){
                                        //requestWeather
                                        requestWeather(weatherId);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }else{
            //query full info from server
            Log.d(TAG, "queryWeather: query full info from server");
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: query province info");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String provinceText = response.body().string();
                    Utility.handleProvinceResponse(provinceText);
                    Log.d(TAG, "onResponse:194 "+provinceText);
                    final int pro_id = queryProvinceIdFromLocal(provinceName);
                    final int cityId = queryCityIdFromLocal(pro_id,cityName);
                    Log.d(TAG, "onResponse: 196 "+cityId);
                    if (cityId != -1){
                        //check if county is null
                        String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                        if (!TextUtils.isEmpty(weatherId)){
                            //requestWeather
                            requestWeather(weatherId);
                        }else{
                            //query weather info from server
                            Log.d(TAG, "queryWeather: query city info from server");
                            HttpUtil.sendOkHttpRequest(address + "/" + provinceId + "/" + cityId, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.d(TAG, "onFailure: request county info");
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String countyText = response.body().string();
                                    Log.d(TAG, "onResponse: "+countyText);
                                    Utility.handleCountyResponse(countyText,cityId);
                                    String weatherId = queryWeatherIdFromLocal(cityId,countyName);
                                    if (!TextUtils.isEmpty(weatherId)){
                                        //requestWeather
                                        requestWeather(weatherId);
                                    }
                                }
                            });
                        }
                    }else {
                        //query city info from server
                        Log.d(TAG, "onResponse: 227 +query city info from server");
                        HttpUtil.sendOkHttpRequest(address + "/" + pro_id, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG, "onFailure: query city info");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String cityText = response.body().string();
                                Log.d(TAG, "onResponse: "+cityText);
                                Utility.handleCityResponse(cityText,pro_id);
                                final int c_id = queryCityIdFromLocal(pro_id,cityName);
                                String weatherId = queryWeatherIdFromLocal(c_id,countyName);
                                if (!TextUtils.isEmpty(weatherId)){
                                    //requestWeather
                                    requestWeather(weatherId);
                                }else{
                                    //query weather info from server
                                    HttpUtil.sendOkHttpRequest(address + "/" + pro_id + "/" + c_id, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.d(TAG, "onFailure: request county info");
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String countyText = response.body().string();
                                            Log.d(TAG, "onResponse: "+countyText);
                                            Utility.handleCountyResponse(countyText,c_id);
                                            String weatherId = queryWeatherIdFromLocal(c_id,countyName);
                                            if (!TextUtils.isEmpty(weatherId)){
                                                //requestWeather
                                                requestWeather(weatherId);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
   }


    private String queryWeatherIdFromLocal( int cityId,  String countyName) {
        List<County> countyList = DataSupport.where("cityId = ?",String.valueOf(cityId)).find(County.class);
        if (countyList.size() > 0){
            for (County county : countyList){
                if (county.getCountyName().equals(countyName)){
                    return county.getWeatherId();
                }
            }
        }
        return null;
    }

    private int queryCityIdFromLocal(  int provinceId,  String cityName) {
        List<City> cities = DataSupport.where("provinceId=?", String.valueOf(provinceId)).find(City.class);
        if (cities.size() > 0){
            for (City city : cities){
                if (city.getCityName().equals(cityName)){
                    return city.getCityCode();
                }
            }
        }
        return -1;
    }
     String address = "http://guolin.tech/api/china";
    private int queryProvinceIdFromLocal( String provinceName) {
        List<Province> provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            for (Province province : provinceList){
                if (province.getProvinceName().equals(provinceName)){
                    return  province.getProvinceCode();
                }
            }
        }
        return -1;
    }

    private void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                +weatherId+"&key=1bd9697783404217b228bfd43d998b15";
        Log.d(TAG, "requestWeather: 314 executed");
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: requestWeather");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
//                Log.d(TAG, "onResponse: "+responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(Main2Activity.this,
                                    "requestWeather failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(Main2Activity.this,Manifest.permission.READ_PHONE_STATE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(Main2Activity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(Main2Activity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initialLocation();
        mLocationClient.start();
    }

    private void initialLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(500000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    private void showWeatherInfo(final Weather weather) {
        final String cityName = weather.basic.cityName;
        runOnUiThread(new Runnable() {
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
                    View view = LayoutInflater.from(Main2Activity.this).inflate(R.layout.forecast_item,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(Main2Activity.this,"requestPermission failed",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(Main2Activity.this,"requestPermission error",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

   public class mBDAbstractLocationListener extends BDAbstractLocationListener{
        private String cur_city,cur_province,cur_county;
//       int provinceId,cityId;
//       String weatherId;
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
//            cur_country = bdLocation.getCountry();
            cur_province = bdLocation.getProvince();
            cur_city = bdLocation.getCity();
            cur_county = bdLocation.getDistrict();

//
            Log.d(TAG, "onReceiveLocation: "+cur_province+" "+cur_city+" "+cur_county);
//            title_city.setText(cur_city);
//            cur_province = "广东";
//            cur_city = "广州";
//            cur_county = "番禺";

            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("cur_province",cur_province);
            bundle.putString("cur_city",cur_city);
            bundle.putString("cur_county",cur_county);
            Log.d(TAG, "onReceiveLocation: "+bundle.toString());
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }
}
