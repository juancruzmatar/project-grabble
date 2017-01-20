package com.s1451552.grabble;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Vytautas on 15/01/2017.
 */

public class LightningDialogViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private String[] data;

    public LightningDialogViewAdapter(Context context, int layoutResourceId,
                           String[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        LinearLayout wordList = null;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(layoutResourceId, parent, false);
            wordList = (LinearLayout) rowView.findViewById(R.id.dialog_word_images);
            rowView.setTag(wordList);

            Log.d("LightningDialogAdapter", "Word at " + position + ": " + data[position]);
            String word = data[position].toLowerCase();

            char[] chWord = word.toCharArray();
            for (int i = 0; i < 7; i++) {
                String resource = "letter_" + chWord[i];
                int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

                ImageView letter = new ImageView(context);
                letter.setForegroundGravity(Gravity.CENTER_HORIZONTAL);

                Picasso.with(context)
                        .load(imageId)
                        .resize(100, 100)
                        .centerCrop()
                        .into(letter);

                wordList.addView(letter);
            }
        } else {
            wordList = (LinearLayout) convertView;

            String word = data[position].toLowerCase();
            char[] chWord = word.toCharArray();
            for (int i = 0; i < 7; i++) {
                String resource = "letter_" + chWord[i];
                int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

                ImageView letter = (ImageView) wordList.getChildAt(i);

                Picasso.with(context)
                        .load(imageId)
                        .resize(100, 100)
                        .centerCrop()
                        .into(letter);
            }
        }
        return wordList;
    }
}
