package com.elapse.easyweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.elapse.easyweather.Main2Activity;
import com.elapse.easyweather.db.County;
import com.elapse.easyweather.gson.Weather;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateWeatherService extends Service {
    private static final String TAG = "UpdateWeatherService";
    public UpdateWeatherService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Bundle b = intent.getExtras();
        if (intent != null){
            Bundle b = intent.getBundleExtra("url_data");
            if (b.getString("weatherUrl") != null){
                String weatherUrl = b.getString("weatherUrl");
                Log.d(TAG, "onStartCommand: "+weatherUrl);
                updateWeather(weatherUrl);
            }
        }

        updatePic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,UpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return Service.START_REDELIVER_INTENT;
    }

    private void updateWeather(String weatherUrl){
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null){
                    SharedPreferences.Editor editor = PreferenceManager.
                            getDefaultSharedPreferences(UpdateWeatherService.this).edit();
                    editor.putString("weather",responseText);
                    editor.apply();
                }
            }
        });
    }

    private void updatePic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        UpdateWeatherService.this).edit();
                editor.putString("bing_pic",responseText);
                editor.apply();
            }
        });
    }
}
