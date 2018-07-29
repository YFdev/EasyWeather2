package com.elapse.easyweather;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.elapse.easyweather.customView.CustomEditText;
import com.elapse.easyweather.db.County;
import com.elapse.easyweather.db.SearchHistory;
import com.elapse.easyweather.utils.AssetsUtils;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class search_Activity extends AppCompatActivity implements View.OnClickListener{

    private ImageView goBack;
    //搜索栏
    private CustomEditText customEditText;
    private Button btn_search;
    private TextView current_city;
    private GridView hot_city;
    private ListView search_history;
    private Button clear_history;
    //历史列表
    private List<String> historyList;
    //初始化热门城市
    private String[] hotCities = {"北京","上海","广州","深圳","西安","武汉","天津"};
    private ArrayAdapter<String> adapter;
    SQLiteDatabase db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        init();
        Intent intent = getIntent();
        String currentCity = intent.getStringExtra("currentCity");
        current_city.setText(currentCity);

        historyList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this,
                R.layout.textitems,historyList);
        search_history.setAdapter(adapter);
        search_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = historyList.get(position).trim();
                feedBackData(cityName);
            }
        });
        ArrayAdapter<String> adapter_hot = new ArrayAdapter<String>(this,
                R.layout.textitems,hotCities);
        hot_city.setAdapter(adapter_hot);
        hot_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = hotCities[position].trim();
                feedBackData(cityName);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadHistoryList();
    }

    private void init(){
        goBack = findViewById(R.id.goBack);
        customEditText = findViewById(R.id.ed_text);
        btn_search = findViewById(R.id.btn_search);
        current_city = findViewById(R.id.current_city);
        hot_city = findViewById(R.id.hot_city);
        search_history = findViewById(R.id.search_history);
        clear_history = findViewById(R.id.clear_history);

        goBack.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        clear_history.setOnClickListener(this);

        db = AssetsUtils.getDataBase();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.goBack:
                finish();
                break;
            case R.id.btn_search:
                String cityName = customEditText.getText().toString().trim();
                feedBackData(cityName);
                break;
            case R.id.clear_history:
                historyList.clear();
                adapter.notifyDataSetChanged();
                DataSupport.deleteAll(SearchHistory.class);
                break;
                default:
                    break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    //从搜索栏获取搜索信息，返回给MainActivity
    private void feedBackData(String cityName) {
        if (!historyList.contains(cityName)){
            historyList.add(cityName);
            adapter.notifyDataSetChanged();
        }
        String weatherId = "";
        Cursor cursor = db.query("county",null,"countyname = ?",
                new String[]{cityName},null,null,null);
        while (cursor.moveToNext()){
             weatherId = cursor.getString(cursor.getColumnIndex("weatherid"));
        }
        cursor.close();
//        List<County> countyList = DataSupport.where("countyName=?",cityName).find(County.class);
//        if (countyList.size() > 0){
//            String weatherId = countyList.get(0).getWeatherId().trim();
            Intent i = new Intent();
            i.putExtra("weatherId",weatherId);
            i.putExtra("cityName",cityName);
            setResult(RESULT_OK,i);
            finish();
//        }else {
//            Toast.makeText(search_Activity.this,"initialize unfinished or input mistake",
//                    Toast.LENGTH_SHORT).show();
//        }
    }
    //从数据库加载历史记录
    private void loadHistoryList(){
        historyList.clear();
//        adapter.notifyDataSetChanged();
        List<SearchHistory> list = DataSupport.findAll(SearchHistory.class);
        if (list.size()>0){
            for (int i = 0;i<list.size();i++){
                SearchHistory  history= list.get(i);
                historyList.add(history.getCityName());
            }
        }
        adapter.notifyDataSetChanged();
    }
    //保存历史记录
    private void saveHistoryList(){
        DataSupport.deleteAll(SearchHistory.class);
        for (int i = 0;i<historyList.size();i++){
            SearchHistory history = new SearchHistory();
            history.setCityName(historyList.get(i));
            history.save();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveHistoryList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
