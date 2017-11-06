package com.hrmaarhus.weatherapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CityDetailsActivity extends AppCompatActivity {

    Button removeBtn, okBtn;
    TextView cityNameTextView, humidityTextView, temperatureTextView, descriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        removeBtn = findViewById(R.id.removeBtn);
        okBtn = findViewById(R.id.okBtn);
        cityNameTextView = findViewById(R.id.cityNameTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
