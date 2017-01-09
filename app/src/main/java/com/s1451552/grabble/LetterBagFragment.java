package com.s1451552.grabble;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

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
    public static LetterBagFragment newInstance(String text) {
        LetterBagFragment fragment = new LetterBagFragment();

        // Passing data from the new instance
        //Bundle bundle = new Bundle();
        //bundle.putString("msg", text);
        //fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_letterbag, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid);
        mGridAdapter = new LetterViewAdapter(getActivity(), R.layout.layout_griditem, getData());
        mGridView.setAdapter(mGridAdapter);


        //TextView textView = (TextView) rootView.findViewById(R.id.section_letterbag);
        //textView.setText(getArguments().getString("msg"));
        return rootView;
    }

    // Prepare some dummy data for gridview
    private ArrayList<String> getData() {
        final ArrayList<String> imageItems = new ArrayList<>();
        for (int i = 65; i <= 90; i++) {
            imageItems.add(String.valueOf((char)i));
        }
        return imageItems;
    }
}
