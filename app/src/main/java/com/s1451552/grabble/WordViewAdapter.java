package com.s1451552.grabble;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * ArrayAdapter to display words as lists of letters
 * in the Lightning Mode dialog.
 */

public class WordViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> data = new ArrayList();

    public WordViewAdapter(Context context, int layoutResourceId,
                             ArrayList<String> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;

        Log.d("WordViewAdapter", "Word at " + position + ": " + data.get(position));
        String word = data.get(position).split("-")[0].toLowerCase();
        String amount = data.get(position).split("-")[1];
        char[] chWord = word.toCharArray();

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) rowView.findViewById(R.id.word_text);
            holder.imageList = (LinearLayout) rowView.findViewById(R.id.word_image_list);
            rowView.setTag(holder);

            for (int i = 0; i < 7; i++) {
                String resource = "letter_" + chWord[i];
                int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

                ImageView letter = new ImageView(context);

                Picasso.with(context)
                        .load(imageId)
                        .resize(120, 120)
                        .centerCrop()
                        .into(letter);

                holder.imageList.addView(letter);
            }

            holder.imageTitle.setText(amount);
            holder.imageTitle.setTextSize(16);
        } else {
            rowView = (RelativeLayout) convertView;
            TextView imageTitle = (TextView) rowView.findViewById(R.id.word_text);
            LinearLayout imageList = (LinearLayout) rowView.findViewById(R.id.word_image_list);

            for (int i = 0; i < 7; i++) {
                String resource = "letter_" + chWord[i];
                int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

                ImageView letter = (ImageView) imageList.getChildAt(i);

                Picasso.with(context)
                        .load(imageId)
                        .resize(120, 120)
                        .centerCrop()
                        .into(letter);
            }

            imageTitle.setText(amount);
            imageTitle.setTextSize(16);
        }
        return rowView;
    }

    static class ViewHolder {
        TextView imageTitle;
        LinearLayout imageList;
    }
}
