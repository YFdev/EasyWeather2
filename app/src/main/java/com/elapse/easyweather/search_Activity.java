package com.elapse.easyweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.elapse.easyweather.R;
import com.elapse.easyweather.db.County;

import org.litepal.crud.DataSupport;

import java.util.List;

import scut.carson_ho.searchview.ICallBack;
import scut.carson_ho.searchview.SearchView;
import scut.carson_ho.searchview.bCallBack;

public class search_Activity extends AppCompatActivity {
    private SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_);
        searchView = findViewById(R.id.search_view);
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                List<County> countyList = DataSupport.where("name=?",string).find(County.class);
                if (countyList.get(0) != null){
                    String weatherId = countyList.get(0).getWeatherId();
                    Intent intent = new Intent(search_Activity.this,Main2Activity.class);
                    intent.putExtra("weatherId",weatherId);
                    startActivity(intent);
                }else {
                    Toast.makeText(search_Activity.this,"invalid input",Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {

            }
        });
    }
}
