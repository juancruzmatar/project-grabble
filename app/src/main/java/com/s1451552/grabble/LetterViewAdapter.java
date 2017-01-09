package com.s1451552.grabble;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vytautas on 08/11/2016.
 */

public class LetterViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> data = new ArrayList();

    public LetterViewAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
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

        int leId = context.getResources().getIdentifier(data.get(position), "string", context.getPackageName());
        String points = context.getResources().getString(leId);

        String resource = "letter_" + data.get(position).toLowerCase();
        int imId = context.getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imId);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);

        holder.imageTitle.setText("(" + points + ")");
        holder.imageTitle.setTextSize(20);
        holder.image.setImageBitmap(scaled);
        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
