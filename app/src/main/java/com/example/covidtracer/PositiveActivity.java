package com.example.covidtracer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PositiveActivity extends AppCompatActivity {
    /**
     * this class displays the amount of time a user has to
     * quarantine.
     * Time is provided from profile fragment and will be shown
     * to the user in a neat way that dynamically changes from
     * days to hours.
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positive);
        TextView num = findViewById(R.id.quarantineTime);
        TextView first = findViewById(R.id.first);
        TextView second = findViewById(R.id.second);
        TextView link = findViewById(R.id.helpfulQuar);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        long quarTime = getIntent().getLongExtra("val", 0);
        //Timer that updates GUI at every tick (second)
        new CountDownTimer(quarTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int day = 0;
                int hour = 0;
                int minute = 0;
                long millsPerDay = 86400000;
                long millsPerHour = 3600000;
                long millsPerMin = 60000;
                String hours;
                String minutes;
                String days;
                //Displaying data based on time remaining
                if (millisUntilFinished / millsPerDay >= 1) {
                    day = (int) (millisUntilFinished / millsPerDay);
                    // equation to get remaining time after day conversion
                    hour = (int) ((millisUntilFinished - (day * millsPerDay)) / millsPerHour);
                    days = "" + day;
                    hours = "" + hour;
                    if (days.length() < 2) {
                        days = "0" + days;
                    }
                    if (hours.length() < 2) {
                        hours = "0" + hours;
                    }
                    num.setText(days + ":" + hours);
                    first.setText("Days");
                    second.setText("Hours");

                } else if (millisUntilFinished / millsPerDay < 1) {
                    hour = (int) (millisUntilFinished / millsPerHour);
                    minute = (int) ((millisUntilFinished - (hour * millsPerHour)) / millsPerMin);
                    hours = "" + hour;
                    minutes = "" + minute;
                    if (hours.length() < 2) {
                        hours = "0" + hours;
                    }
                    if (minutes.length() < 2) {
                        minutes = "0" + minutes;
                    }
                    num.setText(hours + ":" + minutes);
                    first.setText("Hours");
                    second.setText("Minutes");
                }
            }

            @Override
            public void onFinish() {
                num.setText("No need to quarantine");
            }
        }.start();

        num.setText(getIntent().getStringExtra("val"));
    }
}