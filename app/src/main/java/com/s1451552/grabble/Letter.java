package com.s1451552.grabble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Vytautas on 08/11/2016.
 */

public class Letter {

    private char letter;
    private LatLng coordinates;

    public Letter(char letter, LatLng coordinates) {
        this.letter = letter;
        this.coordinates = coordinates;
    };

    public char getLetter() {
        return letter;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public String getPoints(Context context) {
        int id = context.getResources().getIdentifier(String.valueOf(this.letter), "string", context.getPackageName());
        String points = context.getResources().getString(id);
        return points;
    }

    public Bitmap getImage(Context context) {
        String resource = "letter_" + String.valueOf(this.letter).toLowerCase();
        int id = context.getResources().getIdentifier(resource, "drawable", context.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);

        // TODO: make the pictures same sized!!!
        // This line is for testing only
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
        return scaled;
    }

    @Override
    public String toString() {
        return String.valueOf(this.letter);
    }

}
