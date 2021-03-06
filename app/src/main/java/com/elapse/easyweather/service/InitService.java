package com.elapse.easyweather.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.elapse.easyweather.db.City;
import com.elapse.easyweather.db.Province;
import com.elapse.easyweather.utils.HttpUtil;
import com.elapse.easyweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
@Deprecated
//该service用于获取所有省市天气信息并保存，但是运行期间会导致第一次启动时APP执行任务太多
//用法是作为工具类运行一次，获取所有信息后将出具库文件导出到Assets目录，APP运行时将数据库文件拷贝到
//databases目录中即可
public class InitService extends Service {
    private static final String TAG = "InitService";
    ExecutorService executor ;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String address = "http://guolin.tech/api/china";
        new Thread(new Runnable() {
            @Override
            public void run() {
                queryFullInfoAndSave(address);
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void queryFullInfoAndSave(final String address) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_p = response.body().string();
                Utility.handleProvinceResponse(response_p);
                List<Province> provinceList = DataSupport.findAll(Province.class);
                for (Province p:provinceList){
                    final int provinceId = p.getProvinceCode();
                    Runnable command = new Runnable() {
                        @Override
                        public void run() {
                            HttpUtil.sendOkHttpRequest(address + "/" + provinceId, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String response_c = response.body().string();
                                    Utility.handleCityResponse(response_c,provinceId);
                                    final List<City> cityList = DataSupport.findAll(City.class);
                                    for (int i = 0;i<cityList.size();i++){
                                        final int j = i;
                                        City c = cityList.get(i);
                                        final int cityId = c.getCityCode();
                                        Runnable cityCommand = new Runnable() {
                                            @Override
                                            public void run() {
                                                HttpUtil.sendOkHttpRequest(address + "/" + provinceId + "/" + cityId, new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {

                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        String response_county = response.body().string();
                                                        Log.d("TAGGG", "onResponse: "+response_county);
                                                        Utility.handleCountyResponse(response_county,cityId);
                                                    }
                                                });
                                            }
                                        };
                                        executor.execute(cityCommand);
                                    }
                                }
                            });
                        }
                    };
                    executor.execute(command);
                }
                stopSelf();
            }
        });

    }

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        executor.shutdown();
        Log.d(TAG, "onDestroy: initial complete");
    }
}
