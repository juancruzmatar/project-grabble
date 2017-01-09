package com.s1451552.grabble;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Letter Bag fragment for BackpackActivity
 * Will store collected letters and their point values
 * Created by Vytautas on 08/11/2016.
 */

public class LetterBagFragment extends Fragment {

    private GridView mGridView;
    private LetterViewAdapter mGridAdapter;

    public LetterBagFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LetterBagFragment newInstance(ArrayList<String> letters) {
        LetterBagFragment fragment = new LetterBagFragment();

        // Passing data from the new instance
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("letters", letters);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_letterbag, container, false);

        final ArrayList<String> letters = getArguments().getStringArrayList("letters");

        mGridView = (GridView) rootView.findViewById(R.id.letter_grid);
        mGridAdapter = new LetterViewAdapter(getActivity(), R.layout.layout_griditem, letters);
        mGridView.setAdapter(mGridAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });

        return rootView;
    }
}
