package com.s1451552.grabble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.NumberFormat;

import static com.s1451552.grabble.MainActivity.LETTER_COUNT;
import static com.s1451552.grabble.MainActivity.TRAVEL_DISTANCE;
import static com.s1451552.grabble.MainActivity.WORD_COUNT;
import static com.s1451552.grabble.MainActivity.HIGHSCORE;
import static com.s1451552.grabble.MainActivity.preferences;

/**
 * Displays in-game statistics by fetching data
 * from SharedPreferences. Code is pretty much
 * self-explanatory.
 */

public class StatisticsActivity extends AppCompatActivity {
    public static final String TAG = "StatisticsActivity";

    SharedPreferences grabblePref;
    NumberFormat mDecFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        grabblePref = getApplicationContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_statistics);

        TextView txtDistance = (TextView) findViewById(R.id.travel_distance_var);
        TextView txtLetters = (TextView) findViewById(R.id.letter_number_var);
        TextView txtWords = (TextView) findViewById(R.id.word_number_var);
        TextView txtHighscore = (TextView) findViewById(R.id.highscore_var);

        mDecFormatter = NumberFormat.getNumberInstance();
        mDecFormatter.setMinimumFractionDigits(2);
        mDecFormatter.setMaximumFractionDigits(2);

        if (grabblePref.contains(TRAVEL_DISTANCE)) {
            float distance = grabblePref.getInt(TRAVEL_DISTANCE, -1);
            if (distance != 0.0) {
                if (distance < 1000) {
                    String sDistance = String.valueOf(distance) + " meters";
                    txtDistance.setText(sDistance);
                } else {
                    distance = distance / 1000;
                    String sDistance = mDecFormatter.format(distance) + " kilometers";
                    txtDistance.setText(sDistance);
                }
                Log.d(TAG, "Distance: " + distance);
            }
        }
        if (grabblePref.contains(LETTER_COUNT)) {
            txtLetters.setText(String.valueOf(grabblePref.getInt(LETTER_COUNT, -1)));
        }
        if (grabblePref.contains(WORD_COUNT)) {
            txtWords.setText(String.valueOf(grabblePref.getInt(WORD_COUNT, -1)));
        }
        if (grabblePref.contains(HIGHSCORE)) {
            txtHighscore.setText(String.valueOf(grabblePref.getInt(HIGHSCORE, -1)));
        }

        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
