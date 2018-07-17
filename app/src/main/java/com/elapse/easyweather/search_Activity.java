package com.elapse.easyweather;

import android.content.Intent;
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

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class search_Activity extends AppCompatActivity implements View.OnClickListener{

    private ImageView goBack;
    private CustomEditText customEditText;
    private Button btn_search;
    private TextView current_city;
    private GridView hot_city;
    private ListView search_history;
    private Button clear_history;
    private List<String> historyList;
    private String[] hotCities = {"北京","上海","广州","深圳"};
    private ArrayAdapter<String> adapter;
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
                String cityName = historyList.get(position);
                feedBackData(cityName);
            }
        });
        ArrayAdapter<String> adapter_hot = new ArrayAdapter<String>(this,
                R.layout.textitems,hotCities);
        hot_city.setAdapter(adapter_hot);
        hot_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cityName = hotCities[position];
                feedBackData(cityName);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        LitePal.getDatabase();
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

    private void feedBackData(String cityName) {
        if (!historyList.contains(cityName)){
            historyList.add(cityName);
            adapter.notifyDataSetChanged();
        }
        List<County> countyList = DataSupport.where("countyName=?",cityName).find(County.class);
        if (countyList.size() > 0){
            String weatherId = countyList.get(0).getWeatherId();
            Intent i = new Intent();
            i.putExtra("weatherId",weatherId);
            setResult(RESULT_OK,i);
            finish();
        }else {
            Toast.makeText(search_Activity.this,"initial unfinished or input mistake",
                    Toast.LENGTH_SHORT).show();
        }

    }
    private void loadHistoryList(){
        List<SearchHistory> list = DataSupport.findAll(SearchHistory.class);
        if (list.size()>0){
            for (int i = 0;i<list.size();i++){
                SearchHistory  history= list.get(i);
                historyList.clear();
                historyList.add(history.getCityName());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void saveHistoryList(){
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
