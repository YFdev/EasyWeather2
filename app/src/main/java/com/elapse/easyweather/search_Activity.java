package com.elapse.easyweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.elapse.easyweather.R;

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

            }
        });

        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {

            }
        });
    }
}
