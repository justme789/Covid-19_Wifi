package com.example.covidtracer;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SympPopUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symp_pop_up);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView action = findViewById(R.id.checkBoxResult);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        if (getIntent().getIntExtra("val", 0) == 1) {
            action.setText("Thank you for your time!\nYou should get checked since the symptom you " +
                    "have is linked with Covid-19 ");
            MainActivity.contactsToPositive();
        } else
            action.setText("Thank you for your time!\nPlease stay safe and tell us if anything changes!");

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.8));
    }
}
