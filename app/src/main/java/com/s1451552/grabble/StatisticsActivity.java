package com.s1451552.grabble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.NumberFormat;

import static com.s1451552.grabble.MainActivity.DOESNT_EXIST;
import static com.s1451552.grabble.MainActivity.LETTER_COUNT;
import static com.s1451552.grabble.MainActivity.TRAVEL_DISTANCE;
import static com.s1451552.grabble.MainActivity.WORD_COUNT;
import static com.s1451552.grabble.MainActivity.preferences;

import static com.s1451552.grabble.MainActivity.sLastLocation;
import static com.s1451552.grabble.MainActivity.sOldLocation;

public class StatisticsActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    private ActionBar mActionBar;
    private NumberFormat mDecFormatter;

    private float mTravelDistance;
    private int mLetterCount;
    private int mWordCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting stored preferences
        sharedPref = getApplicationContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        storeDistance();

        setContentView(R.layout.activity_statistics);

        TextView tDistance = (TextView) findViewById(R.id.travel_distance_var);

        mDecFormatter = NumberFormat.getNumberInstance();
        mDecFormatter.setMinimumFractionDigits(2);
        mDecFormatter.setMaximumFractionDigits(2);

        if (sharedPref.contains(TRAVEL_DISTANCE)) {
            float distance = sharedPref.getInt(TRAVEL_DISTANCE, -1);
            if (distance != 0.0)
                if (distance < 1000) {
                    tDistance.setText(String.valueOf(distance) + " meters");
                } else {
                    distance = distance / 1000;
                    String sDistance = mDecFormatter.format(distance);
                    tDistance.setText(sDistance + " km");
                }
        }
        if (sharedPref.contains(LETTER_COUNT)) {
            tDistance.setText(String.valueOf(sharedPref.getInt(LETTER_COUNT, -1)));
        }
        if (sharedPref.contains(WORD_COUNT)) {
            tDistance.setText(String.valueOf(sharedPref.getInt(WORD_COUNT, -1)));
        }

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            // Show the Up button in the action bar.
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeDistance() {
        if (sOldLocation != null) {
            int distance = (int) sLastLocation.distanceTo(sOldLocation);
            if (sharedPref.contains(TRAVEL_DISTANCE)) {
                int old = sharedPref.getInt(TRAVEL_DISTANCE, DOESNT_EXIST);
                distance = distance + old;
                sharedPref.edit().putInt(TRAVEL_DISTANCE, distance).apply();
            } else {
                sharedPref.edit().putInt(TRAVEL_DISTANCE, distance).apply();
            }
        }
    }
}
