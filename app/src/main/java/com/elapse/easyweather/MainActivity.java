package com.elapse.easyweather;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.elapse.easyweather.Adapter.DrawerListAdapter;
import com.elapse.easyweather.Adapter.MyPagerStateAdapter;
import com.elapse.easyweather.customView.DrawerItemLayout;
import com.elapse.easyweather.db.PagerList;
import com.elapse.easyweather.utils.AssetsUtils;
import com.elapse.easyweather.utils.WeatherConst;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,
        DrawerListAdapter.onItemOptionsClickListener , DrawerLayout.DrawerListener{
    private static final String TAG = "MainActivity";
    //使用百度定位
    public LocationClient mLocationClient;
    //用于存放从百度定位获取的省市区信息（可以不使用，懒得改）
    public static String[] location = new String[3];
    //用于获取位置信息后刷新主页Fragment
    private Handler mHandler;
    //用于展示天气信息及增减页面
    private ViewPager pager;
    private MyPagerStateAdapter adapter;
    private List<Fragment> fragmentList;
    //用于记录页面指示器index
    public static int itemCount;
    //页面指示器布局
    private LinearLayout indicatorLayout;
    private LinearLayout.LayoutParams params;
    //搜索按钮
    private FloatingActionButton fab;
    //存放viewpager下所有页面的天气id
    private List<String> weatherIdList;
    //设置viewpager最多容纳页面个数
    public static final int MAX_PAGE_COUNT = 5;
    //用于存放背景图片以及天气信息
    public static SharedPreferences prefs;
    //侧滑显示当前列表
    private DrawerLayout mDrawLayout;
    //flag标记drawer是否处于打开状态
    private boolean isDrawerOpened;
    //viewpager侧滑页面中的listview
    public ListView cur_pager_list;
    private List<String> cur_city_list;
    public static DrawerListAdapter drawerListAdapter;
    //当前城市名称
    private TextView cur_location;

    public static MainActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面顶部与背景融合
        if (Build.VERSION.SDK_INT >= 21){
            View decoView = getWindow().getDecorView();
            decoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //初始化百度location
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new mBDAbstractLocationListener());
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        init();
        //请求权限
        requestPermission();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,search_Activity.class);
                intent.putExtra("currentCity",location[2]);
                startActivityForResult(intent,1);
            }
        });
        //监听drawerlayout的状态
        mDrawLayout.addDrawerListener(this);
        drawerListAdapter = new DrawerListAdapter(this,R.layout.draweritems,cur_city_list);
        cur_pager_list.setAdapter(drawerListAdapter);
        //为drawerlayout每一项注册监听器
        drawerListAdapter.setOnItemOptionsClickListener(this);
//        cur_pager_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });

        //加载之前搜索过的页面
        loadPageList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    //初始化，加载主页-->当前位置页面
    private void init() {
        //初始化sharedpreference
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //在onCreate中先加载当前主页
        fragmentList = new ArrayList<>();
        Layout_frag frag1 = new Layout_frag();
        fragmentList.add(frag1);
        pager = findViewById(R.id.pager);
        pager.addOnPageChangeListener(this);
        //设置最大缓存页面
        pager.setOffscreenPageLimit(4);
        adapter = new MyPagerStateAdapter(getSupportFragmentManager(),fragmentList);
        pager.setAdapter(adapter);
        //加载页面指示器
        indicatorLayout = findViewById(R.id.indicator);
        View locate = new View(this);
        locate.setBackgroundResource(R.drawable.homepage_seletor);
        locate.setEnabled(true);
        params = new LinearLayout.LayoutParams(10,10);
        params.rightMargin = 5;
        indicatorLayout.addView(locate,params);

        weatherIdList = new ArrayList<>();
        fab = findViewById(R.id.fab);
        //初始化drawerlayout
        mDrawLayout = findViewById(R.id.myDrawerLayout);
        cur_pager_list = findViewById(R.id.cur_pager_list);
        cur_city_list = new ArrayList<>();
        cur_location = findViewById(R.id.cur_location);
        //初始化数据库-->存放历史搜索记录和当前所有页面
        LitePal.getDatabase();
        //拷贝Assets中准备好的数据库文件
        copyData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    //搜索页面返回的数据
                    String weatherId = data.getStringExtra("weatherId");
                    String cityName = data.getStringExtra("cityName");
                    //清空后重新加载，避免位置错乱
                    weatherIdList.clear();
                    cur_city_list.clear();
                    List<PagerList> pagerSave = DataSupport.findAll(PagerList.class);
                    if (pagerSave.size()>0) {
                        for (PagerList pl : pagerSave) {
                            weatherIdList.add(pl.getWeatherId());
                            cur_city_list.add(pl.getCityName());
                        }
                    }
                    //如果viewpager中已经包含该项，直接跳到该项
                    if (weatherIdList.contains(weatherId)){
                        pager.setCurrentItem(weatherIdList.indexOf(weatherId)+1,true);
                        return;
                    }
                    //Pager中不足5项时直接添加
                    if (fragmentList.size() < MAX_PAGE_COUNT){
                        addPage(weatherId);
                        cur_city_list.add(cityName);
                        drawerListAdapter.notifyDataSetChanged();
                    }else{
                        //设置Pager中页卡最多为5项，超过时替换最后一项
                        weatherIdList.remove(MAX_PAGE_COUNT-2);
                        weatherIdList.add(weatherId);
                        OtherFrag frag1 = (OtherFrag) fragmentList.get(MAX_PAGE_COUNT-1);

                        frag1.requestWeather(weatherId);
                        adapter.notifyDataSetChanged();
                        pager.setCurrentItem(fragmentList.size()-1,true);
                        itemCount = MAX_PAGE_COUNT - 1;
                        cur_city_list.remove(MAX_PAGE_COUNT-2);
                        cur_city_list.add(cityName);
                        drawerListAdapter.notifyDataSetChanged();
                    }
                }
                break;
        }
    }
    //页卡不足5个时添加页卡和指示器，并设置最新添加的页面为当前页面
    private void addPage(String weatherId) {
        OtherFrag frag = new OtherFrag();
        Bundle b = new Bundle();
        b.putString("weatherId",weatherId);
        frag.setArguments(b);
        fragmentList.add(frag);
        adapter.notifyDataSetChanged();
        weatherIdList.add(weatherId);
        View dot = new View(this);
        dot.setBackgroundResource(R.drawable.indicator_seletor);
        indicatorLayout.addView(dot,params);

//        frag.requestWeather(weatherId);
        Log.d(TAG, "addPage: 218-- "+fragmentList.size()+"--"+indicatorLayout.getChildCount());
        pager.setCurrentItem(fragmentList.size()-1,true);
    }
    //一次请求所有危险权限
    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }
    //请求位置
    private void requestLocation() {
        initialLocation();
        mLocationClient.start();
    }
    //初始化baiduLocation
    private void initialLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(600000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,
                                    "requestPermission failed",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(MainActivity.this,
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        //页面被遮挡即保存数据，确保数据有效性
        savePageList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        itemCount = 0;
        mLocationClient.stop();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //此方法用于更新指示器小点的状态
        indicatorLayout.getChildAt(itemCount).setEnabled(false);
        Log.d(TAG, "addPage: 538-- "+position);
        indicatorLayout.getChildAt(position).setEnabled(true);
        itemCount = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //以下三个方法在DrawerLayout子项点击时回调
    //点击取消按钮
    @Override
    public void onCancel(DrawerItemLayout v2) {
//        点击归位
        ObjectAnimator.ofFloat(v2.getChildAt(1), "translationX",
                v2.getTranslationX(), 0).setDuration(800).start();
        v2.isOptionsShown = false;
    }
    //点击删除按钮
    @Override
    public void onDelete(final DrawerItemLayout view, int pos) {
        final int position = pos;
        final ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 0);
        scaleY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                cur_city_list.remove(position);
                drawerListAdapter.notifyDataSetChanged();
                fragmentList.remove(position + 1);
                weatherIdList.remove(position);
                adapter.notifyDataSetChanged();
                pager.setCurrentItem(position,true);
                indicatorLayout.removeViewAt(position + 1);
                indicatorLayout.getChildAt(position).setEnabled(true);
                mDrawLayout.closeDrawer(GravityCompat.END);
//                ObjectAnimator.ofFloat(view, "scaleY", 0, 1).setDuration(50).start();
            }
        });
        scaleY.setDuration(500).start();
    }
    //点击List子项
    @Override
    public void onContentChoose(String cityName) {
        List<PagerList> l = DataSupport.where("cityName=?",cityName).find(PagerList.class);
        if (l.size() > 0){
            String weatherId = l.get(0).getWeatherId();
            mDrawLayout.closeDrawer(GravityCompat.END);
            isDrawerOpened = false;
            pager.setCurrentItem(weatherIdList.indexOf(weatherId)+1,true);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        //save data while drawer open to avoid data mismatch
        Log.d(TAG, "onDrawerOpened: executed");
        savePageList();
//        cur_city_list.clear();
//        final List<PagerList> pagerSave = DataSupport.findAll(PagerList.class);
//        if (pagerSave.size()>0){
//            for (PagerList pl : pagerSave){
//                cur_city_list.add(pl.getCityName());
//            }
//            drawerListAdapter.notifyDataSetChanged();
//        }

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        //关闭选项卡时保存数据，
        Log.d(TAG, "onDrawerClosed: executed");
        for (int i = 0;i<cur_city_list.size();i++){
            DrawerItemLayout d = (DrawerItemLayout) cur_pager_list.getChildAt(i);
            if (d.isOptionsShown){
                this.onCancel(d);
            }
        }

        WeatherConst.currentIndex = -1;
        WeatherConst.oldIndex = -1;
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public class mBDAbstractLocationListener extends BDAbstractLocationListener{
        private String cur_city,cur_province,cur_county;
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            cur_province = bdLocation.getProvince();
            cur_city = bdLocation.getCity();
            cur_county = bdLocation.getDistrict();
            Log.d(TAG, "onReceiveLocation: "+cur_province+" "+cur_city+" "+cur_county);
            if (TextUtils.isEmpty(cur_province) || TextUtils.isEmpty(cur_city)
                    || TextUtils.isEmpty(cur_county)){
                cur_province = "广东";
                cur_city = "深圳";
                cur_county = "深圳";
            }
            location[0] = cur_province;
            location[1] = cur_city;
            location[2] = cur_county;
            cur_location.setText(cur_county);

            Log.d(TAG, "onReceiveLocation: "+location[0]+" "+location[1]+" "+location[2]);
            Message msg0 = new Message();
            Bundle b = new Bundle();
            b.putString("countyName",cur_county);
            msg0.what = WeatherConst.GET_COUNTY;
            msg0.setData(b);
            mHandler.sendMessage(msg0);
        }
    }

    //在主页的fragment中获取handler
    public void setHandler(Handler handler){
        mHandler = handler;
    }

    //保存状态
    private void savePageList(){
        DataSupport.deleteAll(PagerList.class);
       if (weatherIdList.size()>0){
           for (int i = 0;i<weatherIdList.size();i++){
               PagerList list = new PagerList();
               list.setPageNum(i+1);
               list.setWeatherId(weatherIdList.get(i));
               list.setCityName(cur_city_list.get(i));
               list.save();
           }
       }
    }
    //启动时加载
    private void loadPageList(){
        weatherIdList.clear();
        final List<PagerList> pagerSave = DataSupport.findAll(PagerList.class);
        if (pagerSave.size()>0){
            for (PagerList pl : pagerSave){
                OtherFrag frag = new OtherFrag();
                Bundle b = new Bundle();
                b.putString("weatherId",pl.getWeatherId());
                frag.setArguments(b);
                fragmentList.add(frag);
                adapter.notifyDataSetChanged();
                weatherIdList.add(pl.getWeatherId());
                cur_city_list.add(pl.getCityName());
                View dot = new View(MainActivity.this);
                dot.setBackgroundResource(R.drawable.indicator_seletor);
                dot.setEnabled(false);
                indicatorLayout.addView(dot,params);
//                frag.requestWeather(pl.getWeatherId());

            }
//                   adapter.notifyDataSetChanged();
            drawerListAdapter.notifyDataSetChanged();
            pager.setCurrentItem(0);
        }
    }

    //Clickable方法
    public void openList(View view) {
        mDrawLayout.openDrawer(GravityCompat.END);
//        drawerListAdapter.notifyDataSetChanged();
        isDrawerOpened = true;
    }
//第一次安装程序时将Assets目录的数据库文件copy到databases中
    private void copyData(){
        AssetsUtils utils = new AssetsUtils(this);
        try {
            utils.createDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpened){
            mDrawLayout.closeDrawer(GravityCompat.END);
            isDrawerOpened = false;
        }else {
            finish();
        }
    }

    //drawerlayout的listview中，保证一次只能有一项可以滑动展示“删除”按钮
    public void setSingleOption(){
        int index = WeatherConst.oldIndex;
        for (int i = 0;i<cur_city_list.size();i++){
            DrawerItemLayout d = (DrawerItemLayout) cur_pager_list.getChildAt(i);
            if (d.isOptionsShown && i != index){
                WeatherConst.currentIndex = i;
                if (WeatherConst.oldIndex > -1 &&((DrawerItemLayout) cur_pager_list.getChildAt(index)).isOptionsShown){
                    onCancel((DrawerItemLayout) cur_pager_list.getChildAt(index));
                }
                WeatherConst.oldIndex = WeatherConst.currentIndex;
            }
        }
    }
}
