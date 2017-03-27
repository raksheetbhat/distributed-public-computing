package com.example.raksheet.majorproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import pl.pawelkleczkowski.customgauge.CustomGauge;

/**
 * Created by Raksheet on 08-11-2016.
 */

public class InformationActivity extends AppCompatActivity {

    CustomGauge ramGauge,storageGauge;
    TextView ramGaugeText,storageGaugeText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_layout);

        // RAM gauge
        float usedMegs = 40,totalMegs=100;
        float percentRam = (usedMegs/totalMegs)*270;
        ramGauge = (CustomGauge) findViewById(R.id.stats_ram_gauge);
        ramGaugeText = (TextView) findViewById(R.id.stats_ram_text_view);
        ramGauge.setPointSize((int)percentRam);
        ramGaugeText.setText((int)usedMegs+"/"+(int)totalMegs+" GB");

        // Storage gauge
        int totalStorage = 200,usedStorage = 150;
        float percentStorage = ((float)usedStorage/totalStorage)*270;
        storageGauge = (CustomGauge) findViewById(R.id.stats_storage_gauge);
        storageGaugeText = (TextView) findViewById(R.id.stats_storage_text_view);
        storageGauge.setPointSize((int)percentStorage);
        storageGaugeText.setText(usedStorage+"/"+totalStorage+" GB");
    }
}
