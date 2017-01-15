package com.s1451552.grabble;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import static com.s1451552.grabble.MainActivity.HIGHSCORE;
import static com.s1451552.grabble.MainActivity.letter_list;
import static com.s1451552.grabble.MainActivity.preferences;
import static com.s1451552.grabble.MainActivity.word_list;
import static com.s1451552.grabble.SplashActivity.sWordlist;

/**
 * Letter Bag fragment for BackpackActivity
 * Will store collected letters and their point values
 * Created by Vytautas on 08/11/2016.
 */

public class LetterBagFragment extends Fragment {

    SharedPreferences grabblePref;
    SharedPreferences letterlistPref;
    SharedPreferences wordlistPref;

    public static final String WORD_COUNT = "word_count";

    private static final ArrayList<String> sSuccessMessages =
            new ArrayList<>(Arrays.asList(
                    "Congratulations!",
                    "Great job!",
                    "Perfect!",
                    "Top man!",
                    "Niiice!"));

    private WordBagFragment mWordBagFragment;
    private GridView mGridView;

    private Button mBtnGrabble;
    private LetterViewAdapter mGridAdapter;

    private ArrayList<String> mLetters;
    private ArrayList<ImageView> mImageBoxes;
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

        grabblePref = getContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        letterlistPref = getContext().getSharedPreferences(letter_list, Context.MODE_PRIVATE);
        wordlistPref = getContext().getSharedPreferences(word_list, Context.MODE_PRIVATE);

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

        /**
         * Checks if boxes are full and then searches for
         * the word in the word list using binary search.
         */
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
                    StringBuilder wordBuilder = new StringBuilder();
                    for (ImageView image : mImageBoxes) {
                        wordBuilder.append(mLettersInBoxes.get(image).split("-")[0]);
                    }
                    String word = wordBuilder.toString();

                    WordComparator comp = new WordComparator();
                    int index = Collections.binarySearch(sWordlist, word, comp);
                    Log.d("mBtnGrabble", "Binary search in wordlist resulted in index: " + index);

                    if (index > -1) {
                        if (wordlistPref.contains(word)) {
                            Toast.makeText(
                                    getContext(),
                                    ("You already have such word! No go m8"),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            int totalPoints = 0;
                            for (int i = 0; i < 7; i++) {
                                String letter = String.valueOf(word.charAt(i));
                                int letterId = getResources().getIdentifier(letter, "string", getContext().getPackageName());
                                int points = Integer.parseInt(getResources().getString(letterId));
                                totalPoints += points;

                                int currLetterAmount = letterlistPref.getInt(letter, -1);
                                if (currLetterAmount > 0) {
                                    currLetterAmount -= 1;
                                    letterlistPref.edit().putInt(letter, currLetterAmount).apply();
                                }
                            }

                            for (ImageView image : mImageBoxes) {
                                image.setImageDrawable(null);
                            }
                            mImageBoxes.clear();

                            int msgIndex = (int) (Math.random() * 5);

                            Toast.makeText(
                                    getContext(),
                                    (sSuccessMessages.get(msgIndex)
                                            + " You just got " + totalPoints + " points!"),
                                    Toast.LENGTH_SHORT)
                                    .show();

                            Log.d("btnGrabbleOnClick", word + " " + totalPoints);
                            wordlistPref.edit().putInt(word, totalPoints).apply();

                            int prevScore = grabblePref.getInt(HIGHSCORE, 0);
                            int newScore = prevScore + totalPoints;

                            Log.d("btnGrabbleOnClick", "Highscore: " + newScore);
                            grabblePref.edit().putInt(HIGHSCORE, newScore).apply();

                            int count;
                            if (wordlistPref.getAll() != null) {
                                count = wordlistPref.getAll().size();
                            } else {
                                count = 1;
                            }
                            Log.d("btnGrabbleOnClick", "Total word count: " + count);
                            grabblePref.edit().putInt(WORD_COUNT, count).apply();

                            // Update WordBag fragment view with the new word
                            ((BackpackActivity) getActivity()).addWord(word, totalPoints);
                        }
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


        /**
         * TODO: bug!!! letters come back repeated
         * When a letter is clicked on in the bottom box,
         * it comes back to the grid of letters
         */
        for (ImageView box : mImageBoxes) {
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView image = (ImageView) v;
                    if (image.getDrawable() != null) {
                        String letter = mLettersInBoxes.get(image).split("-")[0];
                        int amount = Integer.parseInt(mLettersInBoxes.get(image).split("-")[1]);
                        amount = amount + 1;
                        String sAmount = String.valueOf(amount);
                        String updatedLetter = letter + "-" + sAmount;

                        if (amount > 1)
                            mLetters.remove(mLettersInBoxes.get(image));

                        mLetters.add(updatedLetter);
                        mLettersInBoxes.remove(image);

                        ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        image.setImageDrawable(null);
                    }
                }
            });
        }

        /**
         * When a letter is selected, it is put
         * to the closest free letter box for word formation
         */
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String resource = "letter_" + mLetters.get(position).split("-")[0].toLowerCase();
                Log.d("LetterBagFragment", "Clicked on: " + resource);
                int imId = getContext().getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);
                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), imId);
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);

                for (ImageView image : mImageBoxes) {
                    if (image.getDrawable() == null) {
                        image.setImageBitmap(scaled);
                        String letter = mLetters.get(position).split("-")[0];
                        int amount = Integer.parseInt(mLetters.get(position).split("-")[1]);

                        amount = amount - 1;
                        String sAmount = String.valueOf(amount);
                        String updatedLetter = letter + "-" + sAmount;

                        mLettersInBoxes.put(image, updatedLetter);
                        mLetters.remove(position);

                        if (amount > 0)
                            mLetters.add(position, updatedLetter);

                        ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                        break;
                    }
                }
            }
        });

        return rootView;
    }

    /**
     * Comparator used for binary search
     * in the word list (since word list comes sorted)
     */
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
