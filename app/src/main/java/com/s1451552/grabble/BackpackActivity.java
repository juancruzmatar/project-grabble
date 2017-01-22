package com.s1451552.grabble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Map;

import static com.s1451552.grabble.MainActivity.LETTER_COUNT;
import static com.s1451552.grabble.MainActivity.WORD_COUNT;
import static com.s1451552.grabble.MainActivity.letter_list;
import static com.s1451552.grabble.MainActivity.preferences;
import static com.s1451552.grabble.MainActivity.word_list;

/**
 * Activity, that will display two tabbed fragments:
 * {@link LetterBagFragment} and {@link WordBagFragment}.
 * Upon initialization it is collecting the letter data here.
 */

public class BackpackActivity extends AppCompatActivity {

    SharedPreferences grabblePref;
    SharedPreferences letterlistPref;
    SharedPreferences wordlistPref;

    /**
     * The {@link TabLayout} that show the section tabs.
     */
    TabLayout mTabLayout;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private int mLetterCount;
    private int mWordCount;

    private ArrayList<String> mLetters;
    private ArrayList<String> mWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_backpack);

        grabblePref = getApplicationContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        letterlistPref = getApplicationContext().getSharedPreferences(letter_list, Context.MODE_PRIVATE);
        wordlistPref = getApplicationContext().getSharedPreferences(word_list, Context.MODE_PRIVATE);

        // Get the total letter and word count
        // to display along the tab names.
        mLetterCount = grabblePref.getInt(LETTER_COUNT, 0);
        mWordCount = grabblePref.getInt(WORD_COUNT, 0);

        // Parse letter and word data from SharedPreferences.
        parseLetters();
        parseWords();

        setupActionBar();

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Get collected letters from the SharedPreferences.
     */
    private void parseLetters() {
        mLetters = new ArrayList<>();
        for (int i = 65; i <= 90; i++) {
            int lCount = letterlistPref.getInt(String.valueOf((char)i), -1);
            if (lCount > 0) {
                /**
                 * Formatting of collected letters and their quantities
                 * follows this example pattern: A-5.
                 * It's used for relatively simple string splitting.
                 */
                mLetters.add(String.valueOf((char)i) + "-" + lCount);
            }
            // TESTING
            // mLetters.add(String.valueOf((char)i) + "-" + 10);
        }
    }

    /**
     * Get collected words from the SharedPreferences.
     */
    private void parseWords() {
        mWords = new ArrayList<>();
        Map<String, ?> words = wordlistPref.getAll();
        for (Map.Entry<String, ?> entry : words.entrySet()) {
            String word = entry.getKey();
            int points = Integer.parseInt(entry.getValue().toString());
            /**
             * Formatting of formed words and their point values
             * follows this example pattern: STUDENT-40.
             * It's used for relatively simple string splitting.
             */
            mWords.add(word + "-" + points);
        }
    }

    /**
     * Update the WordBag view once a new word is added.
     * Called from {@link LetterBagFragment}.
     */
    public void addWord(String word, int points) {
        mWords.add(word + "-" + points);
        mWordCount = grabblePref.getInt(WORD_COUNT, 0);
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.getChildAt(1).refreshDrawableState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LetterBagFragment.newInstance(mLetters);
                case 1:
                    return WordBagFragment.newInstance(mWords);
                default:
                    return LetterBagFragment.newInstance(mLetters);
            }
        }

        @Override
        public int getCount() {
            /**
             * Show 2 pages:
             * Letter Bag / Word Bag
             */
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // For a tab, the example format of it's title is:
            // Letter Bag (5)
            switch (position) {
                case 0:
                    return ("Letter Bag (" + mLetterCount + ")");
                case 1:
                    return "Word Bag (" + mWordCount + ")";
            }
            return null;
        }
    }
}
