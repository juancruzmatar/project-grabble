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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.s1451552.grabble.MainActivity.HIGHSCORE;
import static com.s1451552.grabble.MainActivity.LIGHT_GOT;
import static com.s1451552.grabble.MainActivity.LIGHT_REQUIRED;
import static com.s1451552.grabble.MainActivity.isLightningMode;
import static com.s1451552.grabble.MainActivity.letter_list;
import static com.s1451552.grabble.MainActivity.lightningModeCompleted;
import static com.s1451552.grabble.MainActivity.preferences;
import static com.s1451552.grabble.MainActivity.word_list;
import static com.s1451552.grabble.SplashActivity.sWordlist;

/**
 * Letter Bag fragment for BackpackActivity.
 * Displays collected letters and their quantities.
 * Implements functionality for creating new words.
 */

public class LetterBagFragment extends Fragment {
    public final String TAG = "LetterBagFragment";

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

    private GridView mGridView;

    private Button mBtnGrabble;
    private LetterViewAdapter mGridAdapter;

    /**
     * To be displayed on the GridView.
     */
    private ArrayList<String> mLetters;

    /**
     * To be displayed in the bottom boxes for chosen letters.
     */
    private ArrayList<ImageView> mImageBoxes;

    /**
     * Hash table for images in the boxes linked to their string
     * representations.
     */
    private HashMap<ImageView, String> mLettersInBoxes;

    public LetterBagFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LetterBagFragment newInstance(ArrayList<String> letters) {
        LetterBagFragment fragment = new LetterBagFragment();

        // Passing letter data from the new instance.
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

        mBtnGrabble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formTheWord();
            }
        });

        for (ImageView box : mImageBoxes) {
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    putLetterBack(v);
                }
            });
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                putLetterToBox(position);
            }
        });

        return rootView;
    }

    /**
     * Checks if boxes are full and then searches for
     * the word in the word list using binary search.
     */
    private void formTheWord() {
        // Are boxes filled with 7 letters?
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
            String word = wordBuilder.toString(); // Final word

            // Fun begins. Setting up WordComparator to not mind
            // lowercase and uppercase word start and indicate how
            // the wordlist is sorted.
            WordComparator comp = new WordComparator();
            int index = Collections.binarySearch(sWordlist, word, comp);
            Log.d(TAG, "Binary search in wordlist resulted in index: " + index);

            if (index > -1) {
                if (wordlistPref.contains(word)) {
                    Toast.makeText(
                            getContext(),
                            ("You already have that word, friend!"),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    int totalPoints = 0;

                    // Add up the point values of letters used
                    // and remove them from the list in SharedPreferences.
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

                    // Reset letter boxes.
                    for (ImageView image : mImageBoxes) {
                        image.setImageDrawable(null);
                        image.refreshDrawableState();
                    }

                    // Get a random success message.
                    int msgIndex = (int) (Math.random() * 5);

                    Toast.makeText(
                            getContext(),
                            (sSuccessMessages.get(msgIndex)
                                    + " You just got " + totalPoints + " points!"),
                            Toast.LENGTH_SHORT)
                            .show();

                    Log.d(TAG, word + ": " + totalPoints);
                    wordlistPref.edit().putInt(word, totalPoints).apply(); // Save the word

                    int prevScore = grabblePref.getInt(HIGHSCORE, 0);
                    int newScore = prevScore + totalPoints;

                    Log.d(TAG, "Highscore: " + newScore);
                    grabblePref.edit().putInt(HIGHSCORE, newScore).apply(); // Save new highscore

                    // This part of code runs whenever Lightning Mode is active.
                    // Forms two sets of strings of required words and words the player has got
                    // during the mode gameplay. If there is at least one missing, we continue.
                    // If all words have been found, isLightningMode returns false and
                    // appropriate action is taken in MainActivity.
                    if (isLightningMode) {
                        Set<String> requiredWords = grabblePref.getStringSet(LIGHT_REQUIRED, null);
                        Set<String> gotWords = grabblePref.getStringSet(LIGHT_GOT, null);

                        if (gotWords == null) {
                            gotWords = new HashSet<>();
                        }

                        if (requiredWords == null) {
                            // Returns false if no word array was passed for some reason.
                            isLightningMode = false;
                        } else {
                            gotWords.add(word.toLowerCase());
                            Log.d(TAG, "Lightning mode! Currently have words: "
                                    + gotWords.toString());

                            grabblePref.edit().putStringSet(LIGHT_GOT, gotWords).apply();

                            // Initial value (not to be confused with the actual meaning).
                            lightningModeCompleted = true;
                            for (String w : requiredWords) {
                                if (!gotWords.contains(w.toLowerCase())) {
                                    lightningModeCompleted = false;
                                    break;
                                }
                            }

                            if (lightningModeCompleted) {
                                isLightningMode = false;
                            }
                        }
                    }

                    int count;
                    if (wordlistPref.getAll() != null) {
                        count = wordlistPref.getAll().size();
                    } else {
                        count = 1;
                    }
                    Log.d(TAG, "Total word count: " + count);
                    grabblePref.edit().putInt(WORD_COUNT, count).apply(); // Save total word count

                    // Update WordBag fragment view with the new word.
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

    /**
     * When a letter is selected, it is put
     * to the closest free letter box for word formation.
     */
    private void putLetterToBox(int position) {
        String resource = "letter_" + mLetters.get(position).split("-")[0].toLowerCase();
        int imId = getContext().getResources().getIdentifier(resource, "drawable", MainActivity.PACKAGE_NAME);
        Log.d(TAG, "Clicked on: " + resource);

        for (ImageView image : mImageBoxes) {
            if (image.getDrawable() == null) {
                Picasso.with(getContext())
                        .load(imId)
                        .resize(120, 120)
                        .centerCrop()
                        .into(image);

                String letter = mLetters.get(position).split("-")[0];
                int amount = Integer.parseInt(mLetters.get(position).split("-")[1]);

                amount = amount - 1;
                String sAmount = String.valueOf(amount);
                String updatedLetter = letter + "-" + sAmount;
                Log.d(TAG, "Current amount of letter " + letter + ": " + amount);

                // 1. Remove letter from grid with previous quantity value
                // 2. If the amount of the letter is still 1 or more, update it's quantity
                //    to display it (else leave it removed from grid)
                // 3. Put the letter to the box
                mLetters.remove(position);
                if (amount > 0)
                    mLetters.add(position, updatedLetter);
                mLettersInBoxes.put(image, letter);

                ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * When a letter is clicked on in the bottom box,
     * it comes back to the grid of letters.
     */
    private void putLetterBack(View v) {
        ImageView image = (ImageView) v;
        if (image.getDrawable() != null) {
            String letter = mLettersInBoxes.get(image);
            String oldLetter = null;

            // Since letters on grid are unique, find the
            // correct item to put the letter from clicked box
            // back to grid.
            for (String item : mLetters) {
                if (item.split("-")[0].equals(letter)) {
                    oldLetter = item;
                    break;
                }
            }

            // If there was no such letter, make a new placeholder.
            if (oldLetter == null) {
                oldLetter = letter + "-0";
            }

            int amount = Integer.parseInt(oldLetter.split("-")[1]);

            amount = amount + 1;
            String sAmount = String.valueOf(amount);
            String updatedLetter = letter + "-" + sAmount;

            // if. If the letter existed in the grid, get its index
            //     and update the letter in its place
            // else. Just add the letter back to grid.
            if (amount > 1) {
                int oldIndex = mLetters.indexOf(oldLetter);
                mLetters.remove(oldIndex);
                mLetters.add(oldIndex, updatedLetter);
            } else {
                mLetters.add(updatedLetter);
            }

            // Reset the image box.
            mLettersInBoxes.remove(image);
            image.setImageDrawable(null);

            ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
        }
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
