package com.example.covidtracer;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SearchPopUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_pop_up);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView searchResult = findViewById(R.id.searchResult);
        TextView searchResultTitle = findViewById(R.id.searchResultTitle);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (getIntent().getIntExtra("val", 0) == 0) {
            searchResultTitle.setText("Good to go");
            searchResult.setText("This locations is safe and you should be able to maintain your distance. Please stay safe!");
        } else if (getIntent().getIntExtra("val", 0) == 1) {
            searchResultTitle.setText("Maybe not");
            searchResult.setText("This location is a bit crowded and you will find yourself unable to maintain social distancing quite a lot");
        } else {
            searchResultTitle.setText("Definitely Not");
            searchResult.setText("This location is crowded to the brim, going there will guarantee that you're unable to maintain social distancing!");
        }
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.8));
    }
}