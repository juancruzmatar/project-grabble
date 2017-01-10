package com.s1451552.grabble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Splash Activity
 * Displays a splash screen before entering the game
 */

public class SplashActivity extends AppCompatActivity {

    public static ArrayList<String> sWordlist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();

                    sWordlist = new ArrayList<>();
                    try {
                        InputStream input = getResources().openRawResource(R.raw.gdict);

                        BufferedReader br = new BufferedReader(new InputStreamReader(input));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sWordlist.add(line);
                        }
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sleep(2000);
                } catch (Exception e) {
                    Log.e("SplashActivity", e.toString());
                } finally {
                    Intent i = new Intent(SplashActivity.this,
                            MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
}
