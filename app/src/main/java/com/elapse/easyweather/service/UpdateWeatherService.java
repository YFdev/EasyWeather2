package com.elapse.easyweather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.elapse.easyweather.MainActivity;
import com.elapse.easyweather.R;
import com.elapse.easyweather.gson.Weather;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//定时任务，前台服务
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
                Intent intent = new Intent(UpdateWeatherService.this,MainActivity.class);
                PendingIntent pi = PendingIntent.getActivity(UpdateWeatherService.this,
                        0,intent,0);
                Notification notification = new NotificationCompat.Builder(UpdateWeatherService.this)
                        .setContentTitle("EasyWeather")
                        .setContentText(weather != null ? (weather.basic.cityName+"  "+" "+weather.now.more.info
                                +" "+"  当前气温："+
                                weather.now.temputure+"℃"): "-- : -- : --")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pi)
                        .build();
                startForeground(1,notification);
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
