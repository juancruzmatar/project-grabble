package com.s1451552.grabble;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.s1451552.grabble.SplashActivity.sWordlist;

/**
 * Letter Bag fragment for BackpackActivity
 * Will store collected letters and their point values
 * Created by Vytautas on 08/11/2016.
 */

public class LetterBagFragment extends Fragment {

    private static final ArrayList<String> sWordMessages =
            new ArrayList<>(Arrays.asList(
                    "Congratulations!",
                    "Great job!",
                    "Perfect!",
                    "Top man!",
                    "Niiice!"));

    private GridView mGridView;
    private ArrayList<ImageView> mImageBoxes;
    private Button mBtnGrabble;
    private LetterViewAdapter mGridAdapter;

    private ArrayList<String> mLetters;
    private HashMap<ImageView, String> mLettersInBoxes;

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

        mLetters = getArguments().getStringArrayList("letters");

        mLettersInBoxes = new HashMap<>();

        mGridView = (GridView) rootView.findViewById(R.id.letter_grid);
        mGridAdapter = new LetterViewAdapter(getActivity(), R.layout.layout_griditem, mLetters);
        mGridView.setAdapter(mGridAdapter);

        mImageBoxes = new ArrayList<>();
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_1));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_2));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_3));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_4));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_5));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_6));
        mImageBoxes.add((ImageView) rootView.findViewById(R.id.letter_7));

        mBtnGrabble = (Button) rootView.findViewById(R.id.btn_grabble);
        mBtnGrabble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean boxesFull = mImageBoxes.get(0).getDrawable() != null &&
                        mImageBoxes.get(1).getDrawable() != null &&
                        mImageBoxes.get(2).getDrawable() != null &&
                        mImageBoxes.get(3).getDrawable() != null &&
                        mImageBoxes.get(4).getDrawable() != null &&
                        mImageBoxes.get(5).getDrawable() != null &&
                        mImageBoxes.get(6).getDrawable() != null;

                if (boxesFull) {
                    StringBuilder word = new StringBuilder();
                    for (ImageView image : mImageBoxes) {
                        word.append(mLettersInBoxes.get(image));
                    }
                    WordComparator comp = new WordComparator();
                    int index = Collections.binarySearch(sWordlist, word.toString(), comp);
                    Log.d("mBtnGrabble", "Binary search in wordlist resulted in index: " + index);

                    if (index > -1) {
                        int totalPoints = 0;
                        for (int i = 0; i < 7; i++) {
                            String letter = String.valueOf(word.charAt(i));
                            int letterId = getResources().getIdentifier(letter, "string", getContext().getPackageName());
                            int points = Integer.parseInt(getContext().getResources().getString(letterId));
                            totalPoints+=points;
                        }

                        int msgIndex = (int) (Math.random() * 5);

                        Toast.makeText(
                                getContext(),
                                (sWordMessages.get(msgIndex) + " You just got " + totalPoints + " points!"),
                                Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(
                                getContext(),
                                ("Sorry friend, there is no such word :-("),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(
                            getContext(),
                            ("Aren\'t you missing any letters?"),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        for (ImageView box : mImageBoxes) {
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView image = (ImageView) v;
                    if (image.getDrawable() != null) {
                        mLetters.add(mLettersInBoxes.get(image));
                        mLettersInBoxes.remove(image);

                        ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        image.setImageDrawable(null);
                    }
                }
            });
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String resource = "letter_" + mLetters.get(position).toLowerCase();
                int imId = getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imId);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);

                for (ImageView image : mImageBoxes) {
                    if (image.getDrawable() == null) {
                        image.setImageBitmap(scaled);
                        mLettersInBoxes.put(image, mLetters.get(position));
                        mLetters.remove(position);
                        ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        break;
                    }
                }
            }
        });

        return rootView;
    }

    private class WordComparator implements Comparator<String> {
        @Override
        public int compare(String w1, String w2) {
            if(w1.toUpperCase().compareTo(w2.toUpperCase()) > 0)
                return 1;
            else if(w1.toUpperCase().compareTo(w2.toUpperCase()) == 0)
                return 0;
            else
                return -1;
        }
    }
}
