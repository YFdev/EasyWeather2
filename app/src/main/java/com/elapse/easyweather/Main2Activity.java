package com.elapse.easyweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.elapse.easyweather.Adapter.MyFragmentAdapter;
import com.elapse.easyweather.Adapter.MyPagerStateAdapter;
import com.elapse.easyweather.db.PagerList;
import com.elapse.easyweather.utils.WeatherConst;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity implements ViewPager.OnPageChangeListener{
    private static final String TAG = "Main2Activity";

    public LocationClient mLocationClient;

//    private ScrollView weatherLayout;
//    private TextView title_city,titleUpdateTime,degreeText,weatherInfoText,
//            aqiText,pm25Text,comfortText,carWashText,sportText;
//    private LinearLayout forecastLayout;
//    private ImageView bingPic;
//    SharedPreferences prefs;

    public static String[] location = new String[3];
    private Handler mHandler;
    private ViewPager pager;
    MyFragmentAdapter adapter;
    private List<Fragment> fragmentList;
    public static int itemCount;
    private LinearLayout indicatorLayout;
//    private List<View> indicatorList;
    private LinearLayout.LayoutParams params;
    private FloatingActionButton fab;
    private List<String> weatherIdList;
    public static final int MAX_PAGE_COUNT = 5;
//    private Province selectedProvince;
//    private City selectedCity;
//    private County selectedCounty;
//    public static final int GET_LOCATION = 0;
//    public static final int GET_PROVINCE = 1;
//    public static final int GET_CITY = 2;
//    public static final int GET_COUNTY = 3;

//    public SwipeRefreshLayout swipeRefresh;
//    private String weatherId_fresh;

//    Handler mHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            switch (msg.what){
//                case GET_LOCATION:
//                    Log.d(TAG, "handleMessage: "+location.toString());
//                    String provinceName = location[0];
//                    queryProvince(provinceName);
//                    break;
//                case GET_PROVINCE:
//                    String cityName = location[1];
//                    queryCity(cityName);
//                    break;
//                case GET_CITY:
//                    String countyName = location[2];
//                    queryCounty(countyName);
//                    break;
//                case GET_COUNTY:
//                    requestWeather(selectedCounty.getWeatherId());
//                    break;
//            }
//            return false;
//        }
//    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decoView = getWindow().getDecorView();
            decoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new mBDAbstractLocationListener());
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
        requestPermission();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this,search_Activity.class);
                intent.putExtra("currentCity",location[2]);
                startActivityForResult(intent,1);
//                if (itemCount <= 4){
//                    OtherFrag frag = new OtherFrag();
//                    frag.requestWeather("CN101190101");
//                    itemCount = itemCount + 1;
//                    fragmentList.add(frag);
//                    adapter.notifyDataSetChanged();
//                    pager.setCurrentItem(itemCount,true);
//                }else {
//                    fragmentList.remove(itemCount);
//                }
            }
        });
        loadPageList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        itemCount = 0;
    }

    private void init() {
        // add homepage
        fragmentList = new ArrayList<>();
        Layout_frag frag1 = new Layout_frag();
        fragmentList.add(frag1);
        pager = findViewById(R.id.pager);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(4);
        adapter = new MyFragmentAdapter(getSupportFragmentManager(),fragmentList);
        pager.setAdapter(adapter);
        //add homepage indicator
        indicatorLayout = findViewById(R.id.indicator);
        View locate = new View(this);
        locate.setBackgroundResource(R.drawable.homepage_seletor);
        locate.setEnabled(true);
        params = new LinearLayout.LayoutParams(10,10);
        params.rightMargin = 5;
        indicatorLayout.addView(locate,params);
        weatherIdList = new ArrayList<>();
        fab = findViewById(R.id.fab);
        LitePal.getDatabase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    String weatherId = data.getStringExtra("weatherId");
                    weatherIdList.clear();
                    List<PagerList> pagerSave = DataSupport.findAll(PagerList.class);
                    if (pagerSave.size()>0) {
                        for (PagerList pl : pagerSave) {
                            weatherIdList.add(pl.getWeatherId());
                        }
                    }
                    if (weatherIdList.contains(weatherId)){
                        pager.setCurrentItem(weatherIdList.indexOf(weatherId)+1,true);
                        return;
                    }

                    if (fragmentList.size() < MAX_PAGE_COUNT){
                        addPage(weatherId);
                    }else{
                        weatherIdList.remove(MAX_PAGE_COUNT-2);
                        weatherIdList.add(weatherId);
                        OtherFrag frag1 = (OtherFrag) fragmentList.get(MAX_PAGE_COUNT-1);
                        frag1.requestWeather(weatherId);
                        adapter.notifyDataSetChanged();
                        pager.setCurrentItem(fragmentList.size()-1,true);
                    }
                }
                break;
        }
    }

    private void addPage(String weatherId) {
        OtherFrag frag = new OtherFrag();
        frag.requestWeather(weatherId);
        fragmentList.add(frag);
        weatherIdList.add(weatherId);
        View dot = new View(this);
        dot.setBackgroundResource(R.drawable.indicator_seletor);
        indicatorLayout.addView(dot,params);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "addPage: 218-- "+fragmentList.size()+"--"+indicatorLayout.getChildCount());
        pager.setCurrentItem(fragmentList.size()-1,true);
    }


//    private void loadBingPic() {
//        String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String responseText = response.body().string();
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("bing_pic",responseText);
//                editor.apply();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(Main2Activity.this).load(responseText).into(bingPic);
//                    }
//                });
//            }
//        });
//    }

//    private void initView(View view) {
//        swipeRefresh = view.findViewById(R.id.swipe_refresh);
//        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        weatherLayout = view.findViewById(R.id.weather_layout);
//        title_city = view.findViewById(R.id.title_city);
//        titleUpdateTime = view.findViewById(R.id.title_update_time);
//        degreeText = view.findViewById(R.id.degree_text);
//        weatherInfoText = view.findViewById(R.id.weather_info_text);
//        forecastLayout = view.findViewById(R.id.forecast_layout);
//        aqiText = view.findViewById(R.id.aqi_text);
//        pm25Text = view.findViewById(R.id.pm25_text);
//        comfortText = view.findViewById(R.id.comfort_text);
//        carWashText = view.findViewById(R.id.car_wash_text);
//        sportText = view.findViewById(R.id.sport_text);
//        bingPic = view.findViewById(R.id.bing_pic);
//
//        prefs = PreferenceManager.getDefaultSharedPreferences(Main2Activity.this);
//        String bing_pic = prefs.getString("bing_pic",null);
//        if (bing_pic != null){
//            Glide.with(this).load(bing_pic).into(bingPic);
//        }else{
//            loadBingPic();
//        }
//        String weatherString = prefs.getString("weather",null);
//        if (weatherString != null){
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//            weatherId_fresh = weather.basic.weatherId;
//            showWeatherInfo(weather);
//        }
//    }

//    private void queryProvince(String provinceName){
//        List<Province> provinceList = DataSupport.findAll(Province.class);
//        if (provinceList.size() > 0){
//            for (Province p:provinceList){
//                if (p.getProvinceName().equals(provinceName)){
//                    selectedProvince = p;
//                    Message msg1 = new Message();
//                    msg1.what = GET_PROVINCE;
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
//                    msg2.what = GET_CITY;
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
//                    msg3.what = GET_COUNTY;
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

//    private void requestWeather(final String weatherId) {
//        String weatherUrl = "http://guolin.tech/api/weather?cityid="
//                +weatherId+"&key=1bd9697783404217b228bfd43d998b15";
//        Log.d(TAG, "requestWeather: 314 executed");
//        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.d(TAG, "onFailure: requestWeather");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        swipeRefresh.setRefreshing(false);
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String responseText = response.body().string();
////                Log.d(TAG, "onResponse: "+responseText);
//                final Weather weather = Utility.handleWeatherResponse(responseText);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (weather != null && "ok".equals(weather.status)){
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("weather",responseText);
//                            editor.apply();
//                            showWeatherInfo(weather);
//                        }else {
//                            Toast.makeText(Main2Activity.this,
//                                    "requestWeather failed",Toast.LENGTH_SHORT).show();
//                        }
//                        swipeRefresh.setRefreshing(false);
//                    }
//                });
//            }
//        });
//        Bundle b = new Bundle();
//        b.putString("weatherUrl",weatherUrl);
//        Intent intent_update = new Intent(Main2Activity.this, UpdateWeatherService.class);
////        intent_update.putExtras(b);
//        intent_update.putExtra("url_data",b);
//        startService(intent_update);
//
//        Intent intent2 = new Intent(this, InitService.class);
//        startService(intent2);
//    }

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
        option.setScanSpan(600000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

//    private void showWeatherInfo(final Weather weather) {
//        final String cityName = weather.basic.cityName;
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                title_city.setText(cityName);
//                String updateTime = weather.basic.update.updateTime.split(" ")[1];
//                String degree = weather.now.temputure+"℃";
//                String weatherInfo = weather.now.more.info;
//                titleUpdateTime.setText(updateTime);
//                degreeText.setText(degree);
//                weatherInfoText.setText(weatherInfo);
//                forecastLayout.removeAllViews();
//                for (Forecast forecast:weather.forecastList){
//                    View view = LayoutInflater.from(Main2Activity.this).inflate(R.layout.forecast_item,
//                            forecastLayout,false);
//                    TextView dateText = view.findViewById(R.id.date_text);
//                    TextView infoText = view.findViewById(R.id.info_text);
//                    TextView maxText = view.findViewById(R.id.max_text);
//                    TextView minText = view.findViewById(R.id.min_text);
//                    dateText.setText(forecast.date);
//                    infoText.setText(forecast.more.info);
//                    maxText.setText(forecast.temperature.max);
//                    minText.setText(forecast.temperature.min);
//                    forecastLayout.addView(view);
//                }
//                if (weather.aqi != null){
//                    aqiText.setText(weather.aqi.city.aqi);
//                    pm25Text.setText(weather.aqi.city.pm25);
//                }
//                String comfort = "舒适度："+weather.suggestion.comfort.info;
//                String carWash = "洗车指数："+weather.suggestion.carWash.info;
//                String sport = "运动指数："+weather.suggestion.sport.info;
//                comfortText.setText(comfort);
//                carWashText.setText(carWash);
//                sportText.setText(sport);
//                weatherLayout.setVisibility(View.VISIBLE);
//            }
//        });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(Main2Activity.this,
                                    "requestPermission failed",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(Main2Activity.this,
                            "requestPermission error",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePageList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        indicatorLayout.getChildAt(itemCount).setEnabled(false);
        Log.d(TAG, "addPage: 538-- "+position);
        indicatorLayout.getChildAt(position).setEnabled(true);
        itemCount = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class mBDAbstractLocationListener extends BDAbstractLocationListener{
        private String cur_city,cur_province,cur_county;
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            cur_province = bdLocation.getProvince();
            cur_city = bdLocation.getCity();
            cur_county = bdLocation.getDistrict();
            Log.d(TAG, "onReceiveLocation: "+cur_province+" "+cur_city+" "+cur_county);
//           title_city.setText(cur_city);
            if (TextUtils.isEmpty(cur_province) || TextUtils.isEmpty(cur_city)
                    || TextUtils.isEmpty(cur_county)){
                cur_province = "广东";
                cur_city = "深圳";
                cur_county = "深圳";
            }
            location[0] = cur_province;
            location[1] = cur_city;
            location[2] = cur_county;
            Log.d(TAG, "onReceiveLocation: "+location[0]+" "+location[1]+" "+location[2]);
            Message msg0 = new Message();
            msg0.what = WeatherConst.GET_LOCATION;
            mHandler.sendMessage(msg0);
        }
    }

    public void setHandler(Handler handler){
        mHandler = handler;
    }

    private void savePageList(){
        DataSupport.deleteAll(PagerList.class);
       if (weatherIdList.size()>0){
           for (int i = 0;i<weatherIdList.size();i++){
               PagerList list = new PagerList();
               list.setPageNum(i+1);
               list.setWeatherId(weatherIdList.get(i));
               list.save();
           }
       }
    }

    private void loadPageList(){
        weatherIdList.clear();
        List<PagerList> pagerSave = DataSupport.findAll(PagerList.class);
        if (pagerSave.size()>0){
//            List<OtherFrag> frags = new ArrayList<>();
            for (PagerList pl : pagerSave){
                OtherFrag frag = new OtherFrag();
                frag.requestWeather(pl.getWeatherId());
                fragmentList.add(frag);
                weatherIdList.add(pl.getWeatherId());
                adapter.notifyDataSetChanged();

                View dot = new View(this);
                dot.setBackgroundResource(R.drawable.indicator_seletor);
                dot.setEnabled(false);
                indicatorLayout.addView(dot,params);
            }
            pager.setCurrentItem(0);
        }
    }
}
