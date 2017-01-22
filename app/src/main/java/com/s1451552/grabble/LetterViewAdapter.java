package com.s1451552.grabble;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * ArrayAdapter to display letter list on grid
 * in the {@link LetterBagFragment}.
 */

public class LetterViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> data;

    public LetterViewAdapter(Context context, int layoutResourceId,
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

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) rowView.findViewById(R.id.letter_text);
            holder.image = (ImageView) rowView.findViewById(R.id.letter_image);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
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

        return rowView;
    }

    private static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
