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
import android.widget.TextView;

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
        View row = convertView;
        LinearLayout wordList = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            wordList = (LinearLayout) row.findViewById(R.id.dialog_word_images);
            row.setTag(wordList);
        } else {
            wordList = (LinearLayout) row.getTag();
        }

        Log.d("LightningDialogAdapter", "Word at " + position + ": " + data[position]);

        String word = data[position].toLowerCase();

        for (char l : word.toCharArray()) {
            String resource = "letter_" + l;
            int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageId);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

            ImageView letter = new ImageView(context);
            letter.setImageBitmap(scaled);

            wordList.addView(letter);
        }

        return row;
    }
}
