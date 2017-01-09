package com.s1451552.grabble;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Word Bag fragment for BackpackActivity
 * Will store created words and their point values
 * Created by Vytautas on 08/11/2016.
 */

public class WordBagFragment extends Fragment {

    public WordBagFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WordBagFragment newInstance(String text) {
        WordBagFragment fragment = new WordBagFragment();
        Bundle bundle = new Bundle();
        bundle.putString("msg", text);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wordbag, container, false);

        //TextView textView = (TextView) rootView.findViewById(R.id.section_letterbag);
        //textView.setText(getArguments().getString("msg"));
        return rootView;
    }
}
