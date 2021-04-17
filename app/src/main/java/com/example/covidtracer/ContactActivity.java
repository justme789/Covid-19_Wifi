package com.example.covidtracer;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ContactActivity extends AppCompatActivity {
    /**
     * this activity handles the GUI for the amount of contacts made
     * by the user in the past 24 hours
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        TextView num = findViewById(R.id.contactsToday);
        TextView link = findViewById(R.id.link);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        num.setText(getIntent().getStringExtra("val"));
    }
}