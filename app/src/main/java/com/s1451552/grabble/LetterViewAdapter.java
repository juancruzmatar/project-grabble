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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vytautas on 08/11/2016.
 */

public class LetterViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> data = new ArrayList();

    public LetterViewAdapter(Context context, int layoutResourceId,
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
            holder.imageTitle = (TextView) row.findViewById(R.id.letter_text);
            holder.image = (ImageView) row.findViewById(R.id.letter_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String letter = data.get(position).split("-")[0].toLowerCase();
        String amount = data.get(position).split("-")[1];

        String resource = "letter_" + letter;
        int imageId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);

        Picasso.with(getContext())
                .load(imageId)
                .resize(120, 120)
                .centerCrop()
                .into(holder.image);

        holder.imageTitle.setText(amount);
        holder.imageTitle.setTextSize(16);

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
