package com.s1451552.grabble;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vytautas on 12/01/2017.
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
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.word_text);
            holder.imageList = (LinearLayout) row.findViewById(R.id.word_image_list);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String word = data.get(position).split("-")[0].toLowerCase();
        String amount = data.get(position).split("-")[1];

        for (char l : word.toCharArray()) {
            String resource = "letter_" + l;
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

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        LinearLayout imageList;
    }
}
