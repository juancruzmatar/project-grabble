package com.s1451552.grabble;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyBearingTracking;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.s1451552.grabble.kmlparser.NavigationSaxHandler;
import com.s1451552.grabble.kmlparser.Placemark;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.s1451552.grabble.SplashActivity.sWordlist;

/*
 * This is the core activity. It serves the following purposes:
 *  {*} Displaying main content with loaded map
 *  {*} Downloading and parsing letter placemark map
 *  {*} Displaying letters depending on their distance to the user's location point
 *  {*} Implementing letter pickup and storing the statistics to SharedPreferences
 *  {*} Lightning Mode functionality
 *
 * Written by Vytautas Mizgiris, S1451552
 * Nov 2016 - Jan 2017
 *
 * Some open-source and tutorial code borrowed from:
 * Mapbox: https://www.mapbox.com/android-sdk/
 * Fused location provider: http://www.androidwarriors.com/2015/10/fused-location-provider-in-android.html
 * GridView tutorial code: http://stacktips.com/tutorials/android/android-gridview-example-building-image-gallery-in-android
 * Checking if GPS/Internet is enabled: http://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled
 * ... and others
 */

public class MainActivity extends RuntimePermissions implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public final String TAG = "MainActivity";

    private static final int REQUEST_PERMISSIONS = 20;
    public static String PACKAGE_NAME;

    /* Main shared preferences */
    SharedPreferences grabblePref;
    SharedPreferences letterlistPref;
    SharedPreferences lightLetterlistPref;
    SharedPreferences wordlistPref;
    SharedPreferences settingsPref;

    /* Constants for accessing shared preference data */
    public static final int DOESNT_EXIST = -1;
    public static final String preferences = "grabble_preferences";
    public static final String TRAVEL_DISTANCE = "travel_distance";
    public static final String LETTER_COUNT = "letter_count";
    public static final String WORD_COUNT = "word_count";
    public static final String HIGHSCORE = "highscore";
    public static final String LIGHT_REQUIRED = "lightning_points_required";
    public static final String LIGHT_GOT = "lightning_points_got";

    /* Gameplay data (letters, words, etc) */
    public static final String letter_list = "grabble_letterlist";
    public static final String lightning_letter_list = "grabble_light_letterlist";
    public static final String word_list = "grabble_wordlist";

    /* Layout items */
    ActionBar mActionBar;
    AlertDialog mLightningDialog;
    private MapView mapView;
    private NavigationView mNavigationView;
    private ProgressDialog mProgressDialog;
    private FloatingActionButton mLightningButton;
    private TextView mCountdown;

    /* Variables dealing with auto / manual night mode */
    private final DaytimeChangeReceiver mDaytimeReceiver = new DaytimeChangeReceiver();
    private int dayHour;
    public static boolean isLightningMode;
    public static boolean lightningModeCompleted;
    private static boolean isNightChecked;
    private static boolean isNightAuto;

    /* Mapbox */
    private MapboxMap map;
    private CameraPosition position;

    /* Location */
    Location mLastLocation;
    Location mOldLocation;
    LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    /* Parsed and stored letter and word data */
    private String[] mLightningWords;
    private ArrayList<MarkerViewOptions> mParsedMarkersRaw;
    private ArrayList<Marker> mParsedMarkers;
    private ArrayList<Marker> mLettersAround;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Mapbox.
        MapboxAccountManager.start(this, getString(R.string.mapbox_access_token));

        // Getting stored preferences.
        grabblePref = getApplicationContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        letterlistPref = getApplicationContext().getSharedPreferences(letter_list, Context.MODE_PRIVATE);
        lightLetterlistPref = getApplicationContext().getSharedPreferences(lightning_letter_list, Context.MODE_PRIVATE);
        wordlistPref = getApplicationContext().getSharedPreferences(word_list, Context.MODE_PRIVATE);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Get the current hour of the day to set the map colours initially.
        dayHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        setContentView(R.layout.activity_main);

        // Hide the Action Bar for design purposes
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.hide();
        }

        // Create the Mapbox map view
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Set up side navigation menu
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        // Is navigation menu item "Night mode" toggled on?
        // If in manual night mode switching, initial value will be false.
        isNightChecked = false;
        setupNavigationMenu();

        // Field for showing countdown for Lightning Mode.
        mCountdown = (TextView) findViewById(R.id.countdown);
        mLightningButton = (FloatingActionButton) findViewById(R.id.start_lightning);
        mLightningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareLightningMode();
            }
        });

        // Message that will display if letter data is being downloaded.
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Downloading map data...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mLettersAround = new ArrayList<>();
        mParsedMarkers = new ArrayList<>();
        mParsedMarkersRaw = new ArrayList<>();

        // Request permissions for location access
        // (directing to the class RuntimePermissions)
        // Redirects to onPermissionsGranted(id).
        requestAppPermissions(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION},
                R.string.msg_permissions, REQUEST_PERMISSIONS);
    }

    private void setupNavigationMenu() {
        if (mNavigationView != null)
            mNavigationView.getMenu().findItem(R.id.nav_nightmode).setVisible(!isNightAuto);

        // Setting up selected item listener for the navigation menu.
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        // Checking which item was selected - taking corresponding action.
                        switch (menuItem.getItemId()) {
                            case R.id.nav_backpack: {
                                Log.d(this.toString(), "Pressed backpack");

                                Intent i = new Intent(MainActivity.this, BackpackActivity.class);
                                startActivity(i);
                                return true;
                            }
                            case R.id.nav_statistics: {
                                Log.d(this.toString(), "Pressed stats");

                                Intent i = new Intent(MainActivity.this, StatisticsActivity.class);
                                startActivity(i);
                                return true;
                            }
                            case R.id.nav_settings: {
                                Log.d(this.toString(), "Pressed settings");

                                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(i);
                                return true;
                            }
                            case R.id.nav_nightmode: {
                                if(!isNightChecked) {
                                    menuItem.setChecked(true);
                                    if (map != null)
                                        map.setStyleUrl(getString(R.string.mapbox_mapref_night));
                                    isNightChecked = true;
                                } else {
                                    menuItem.setChecked(false);
                                    if (map != null)
                                        map.setStyleUrl(getString(R.string.mapbox_mapref));
                                    isNightChecked = false;
                                }

                                return true;
                            }
                        }
                        return true;
                    }
                });
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        // Register daytime change receiver.
        registerReceiver(mDaytimeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        // Set map colours if night mode is set to auto.
        setMapColours();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        // Unregister daytime change receiver.
        try {
            unregisterReceiver(mDaytimeReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                Log.w(TAG, "Tried to unregister the receiver when it's not registered");
            } else { throw e; }
        }

        this.writeLetterMap();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        // Save the already *used* letter map.
        this.writeLetterMap();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPermissionsGranted(final int requestCode) {
        buildGoogleApiClient();

        // Start by initializing the Mapbox map.
        initMap();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(2000) // Update location every 2 seconds
                .setFastestInterval(500);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;

            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {}

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {}

            if(!gps_enabled && !network_enabled) {
                // Notify user that he has no internet or GPS connection
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(R.string.msg_gps_disabled_title);
                dialog.setMessage(getString(R.string.msg_gps_disabled));
                dialog.setPositiveButton(getString(R.string.msg_goto_location_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.show();
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                if (map != null) {
                    position = new CameraPosition.Builder()
                            .target(new LatLng(mLastLocation))
                            .zoom(20)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
                    displayClosestLetters(mLastLocation);
                }
            }
        } else {

            // What can the app do when user denies the permissions?
            requestAppPermissions(new String[]{
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},
                    R.string.msg_permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mOldLocation != null)
            mOldLocation = mLastLocation;
        else
            mOldLocation = location;

        mLastLocation = location;

        // Move camera to the player's position,
        // display letters in close proximity
        if (location != null) {
            if (map != null) {
                position = new CameraPosition.Builder()
                        .target(new LatLng(location))
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
                displayClosestLetters(location);
            }

            // Calculate the total distance travelled
            if (mOldLocation != null) {
                int distance = (int) mLastLocation.distanceTo(mOldLocation);
                if (grabblePref.contains(TRAVEL_DISTANCE)) {
                    int old = grabblePref.getInt(TRAVEL_DISTANCE, DOESNT_EXIST);
                    distance = distance + old;
                    grabblePref.edit().putInt(TRAVEL_DISTANCE, distance).apply();
                } else {
                    grabblePref.edit().putInt(TRAVEL_DISTANCE, distance).apply();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please reconnect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network connection was lost. Please reconnect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
    }

    private void initMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                Log.d("initMap", "Map ready, initializing map settings...");
                // Initialize MapboxMap object.
                map = mapboxMap;

                // Game styling requires map to be zoomed in.
                map.setMinZoom(18);
                map.setMaxZoom(20);

                // Enable user tracking to show the padding effect.
                map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.GPS);
                map.getTrackingSettings().setDismissAllTrackingOnGesture(false);

                // Customize the user location icon using the getMyLocationViewSettings object.
                map.getMyLocationViewSettings().setPadding(0, 500, 0, 0);
                map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#efca5b"));
                map.getMyLocationViewSettings().setAccuracyTintColor(0);
                map.getMyLocationViewSettings().setAccuracyAlpha(1);

                // Set map colours if night mode is set to auto.
                setMapColours();

                // What does the application do when a marker is selected?
                map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        collectLetter(marker);
                        return false;
                    }
                });

                // Once map is initialized, do letter scatter map initialization.
                initLetterMapLoad();
            }
        });
    }

    private String storeCurrentWeekday() {
        String PREF_WEEKDAY_KEY = "weekday";
        String DOESNT_EXIST = "empty";

        // Get current day
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        c.setTime(date);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
        String txtWeekday = "";

        switch (weekday) {
            case Calendar.MONDAY: txtWeekday = "monday"; break;
            case Calendar.TUESDAY: txtWeekday = "tuesday"; break;
            case Calendar.WEDNESDAY: txtWeekday = "wednesday"; break;
            case Calendar.THURSDAY: txtWeekday = "thursday"; break;
            case Calendar.FRIDAY: txtWeekday = "friday"; break;
            case Calendar.SATURDAY: txtWeekday = "saturday"; break;
            case Calendar.SUNDAY: txtWeekday = "sunday"; break;
        }

        String storedValue = grabblePref.getString(PREF_WEEKDAY_KEY, DOESNT_EXIST);

        Log.d("storeCurrentWeekday", ("STORED: " + storedValue + ", NEW: " + txtWeekday));

        if (!storedValue.equals(txtWeekday)) {
            SharedPreferences.Editor editor = grabblePref.edit();
            editor.putString(PREF_WEEKDAY_KEY, txtWeekday);
            editor.apply();
            return txtWeekday;
        }

        return "unchanged";
    }

    public void initLetterMapLoad() {
        final String TAG = "initLetterMapLoad";

        // Stores the current day for the map update
        // and returns if the day has changed
        String newDay = storeCurrentWeekday();

        if (!newDay.equals("unchanged")) {
            Log.d(TAG, "Date has changed, downloading new letter map...");

            // Launching the downloader for the map placemark data
            final AsyncTask<String, Integer, String> downloadTask = new DownloadTask(
                    new AsyncResponse() {
                        @Override
                        public void processFinish(String output) {
                            parseLetterMap();
                        }
                    }).execute(
                    "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/" + newDay + ".kml");

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                }
            });
        } else {
            // Points to the cached day and letter map
            String currentDay = grabblePref.getString("weekday", "empty");
            File letterMap = new File(getExternalFilesDir(null), "map.grabble");

            if (letterMap.exists()) {
                Log.d(TAG, "Date hasn't changed, letter map exists!");
                parseLetterMap();
            } else {
                Log.d(TAG, "Date hasn't changed, no letter map found, downloading...");
                // Same procedure for the cached day
                final AsyncTask<String, Integer, String> downloadTask = new DownloadTask(
                        new AsyncResponse() {
                            @Override
                            public void processFinish(String output) {
                                parseLetterMap();
                            }
                        }).execute(
                        "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/" + currentDay + ".kml"
                );

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }
        }
    }

    private interface AsyncResponse {
        void processFinish(String output);
    }

    /**
     * This download task {@link AsyncTask} is responsible for
     * downloading map data from the University server and parsing
     * KML data before passing it to the JSON writer {@code writeLetterMap()}.
     */
    private class DownloadTask extends AsyncTask<String, Integer, String> {
        private final String TAG = "DownloadTask";

        private AsyncResponse delegate = null;
        private PowerManager.WakeLock mWakeLock;

        private DownloadTask(AsyncResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            FileOutputStream output = null;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(sUrl[0]);
                Log.d(TAG, "Getting data from URL: [" + url + "]");

                conn = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "Connecting to the data source...");
                conn.connect();

                // Expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file.
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + conn.getResponseCode()
                            + " " + conn.getResponseMessage();
                } else {
                    Log.d(TAG, "Connected!");
                }

                // Download the file
                input = conn.getInputStream();

                // Get a SAXParser from the SAXPArserFactory
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();

                // Get the XMLReader of the SAXParser we created
                XMLReader xr = sp.getXMLReader();

                // Create a new ContentHandler and apply it to the XML-Reader
                NavigationSaxHandler navSax2Handler = new NavigationSaxHandler();
                xr.setContentHandler(navSax2Handler);

                // Parse the xml-data from the file
                Log.d(TAG, "Parsing downloaded data...");
                xr.parse(new InputSource(input));

                Log.d(TAG, "Getting data from Parser...");
                // Our NavigationSaxHandler now provides the parsed data to us
                ArrayList<Placemark> placemarks = navSax2Handler.getParsedData();

                // Will be of JSON format for fast read/write
                JsonObject mapFile = Json.object();

                int psize = placemarks.size();
                int i = 0;
                for (Placemark p : placemarks) {
                    // Allow cancelling with back button.
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }

                    JsonObject coordinates = Json.object()
                            .add("latitude", p.getCoordinates().getLatitude())
                            .add("longitude", p.getCoordinates().getLongitude());

                    mapFile.add(p.getDescription(), coordinates);

                    i = i + 1;
                    // Publishing the progress....
                    publishProgress((int) (i * 50 / psize));
                }

                // Convert the map JSON object to a byte array.
                InputStream jsonMap = new ByteArrayInputStream(mapFile.toString().getBytes(StandardCharsets.UTF_8));
                // Specify the directory for letter map file storage.
                output = new FileOutputStream(getExternalFilesDir(null) + "/map.grabble");

                byte data[] = new byte[4096];
                int count = 0;
                long total = 0;
                int jsonLength = mapFile.toString().length();
                while (count != -1) {
                    // Allow cancelling with back button.
                    if (isCancelled()) {
                        output.close();
                        return null;
                    }
                    total += count;

                    // Publishing the progress....
                    publishProgress((int) (total * 50 / jsonLength) + 50);
                    output.write(data, 0, count);
                    count = jsonMap.read(data);
                }

            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException e) {
                    // ignored
                }
                if (conn != null)
                    conn.disconnect();

                Log.d(TAG, "Letter map file was successfully downloaded.");
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Take CPU lock to prevent CPU from going off if the user
            // presses the power button during download.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // If we get here, length is known, now set indeterminate to false.
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(MainActivity.this, "Download error! "+ result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(MainActivity.this, "Navigation data downloaded.", Toast.LENGTH_SHORT).show();
            }
            // Send the listener the finishing event,
            // download task has finished.
            delegate.processFinish(result);
        }
    }

    private void parseLetterMap() {
        File mapfile = new File(getApplicationContext().getExternalFilesDir(null), "map.grabble");

        // Read the JSON letter map that was stored in the filesystem.
        try {
            FileReader reader = new FileReader(mapfile);
            JsonValue letters = Json.parse(reader);

            for (JsonObject.Member member : letters.asObject()) {
                char letter = member.getName().toCharArray()[0];
                JsonObject coordinates = member.getValue().asObject();
                double latitude = coordinates.get("latitude").asDouble();
                double longitude = coordinates.get("longitude").asDouble();
                LatLng finalCoordinates = new LatLng(latitude, longitude);

                MarkerViewOptions mvo = new MarkerViewOptions()
                        .position(finalCoordinates)
                        .title(String.valueOf(letter));

                /* Add markers to two arrays for the purpose of watching
                 * if there are markers around and finding the correct ones
                 * to remove from the map.
                 *
                 * Mapbox only has methods:
                 * mapview.add(MarkerViewOptions() type object)
                 * mapview.remove(Marker() type object)
                */
                mParsedMarkersRaw.add(mvo);
                mParsedMarkers.add(mvo.getMarker());

            }
        } catch (Exception e) {
            Log.e("parseLetterMap", e.toString());
        }

    }

    private void writeLetterMap() {
        JsonObject mapFile = Json.object();
        for (MarkerViewOptions m : mParsedMarkersRaw) {
            JsonObject coordinates = Json.object()
                    .add("latitude", m.getPosition().getLatitude())
                    .add("longitude", m.getPosition().getLongitude());

            mapFile.add(m.getTitle(), coordinates);
        }
        InputStream jsonMap = new ByteArrayInputStream(mapFile.toString().getBytes(StandardCharsets.UTF_8));
        OutputStream output = null;
        try {
            output = new FileOutputStream(getExternalFilesDir(null) + "/map.grabble");

            byte data[] = new byte[4096];
            int count = 0;
            while (count != -1) {
                output.write(data, 0, count);
                count = jsonMap.read(data);
            }
        } catch (Exception e) {
            Log.e("writeLetterMap", e.toString());
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                // ignored
            }
            Log.d("writeLetterMap", "Letter map was successfully stored.");
        }
    }

    private void collectLetter(Marker marker) {
        String letter = marker.getTitle();
        Toast.makeText(
                MainActivity.this,
                ("Captured letter " + letter + "!"),
                Toast.LENGTH_SHORT)
                .show();

        // Remove the letter from the map first, then logic.
        map.removeMarker(marker);

        // Checks if the parsed marker array has the marker.
        // Else ignore.
        if (mParsedMarkersRaw != null &&
                mParsedMarkers != null &&
                mParsedMarkers.contains(marker)) {
            /**
             * 1. Check if marker exists in MARKER array;
             * 2. Get index of the marker in MARKER array;
             * 3. Remove marker from MARKER array;
             * 4. Remove marker from MVO array (Raw) by index.
             * 5. Set the current letter count in SharedPreferences
             */
            int markerAtIndex = mParsedMarkers.indexOf(marker);
            mParsedMarkers.remove(marker);
            mParsedMarkersRaw.remove(markerAtIndex);

            SharedPreferences.Editor editor = letterlistPref.edit();

            int currentCount = letterlistPref.getInt(marker.getTitle(), -1);
            if (currentCount != -1) {
                currentCount = currentCount + 1;
                editor.remove(marker.getTitle()).apply();
                editor.putInt(marker.getTitle(), currentCount).apply();
            } else {
                editor.putInt(marker.getTitle(), 1).apply();
            }

            if (isLightningMode) {
                int currentLightCount = lightLetterlistPref.getInt(marker.getTitle(), -1);
                if (currentLightCount != -1) {
                    currentLightCount = currentLightCount + 1;
                    editor.remove(marker.getTitle()).apply();
                    editor.putInt(marker.getTitle(), currentLightCount).apply();
                } else {
                    editor.putInt(marker.getTitle(), 1).apply();
                }
            }

            int count;
            if (letterlistPref.getAll() != null) {
                count = letterlistPref.getAll().size();
            } else {
                count = 1;
            }
            grabblePref.edit().putInt(LETTER_COUNT, count).apply();
            Log.d("collectLetter", "Current captured letter count: " + String.valueOf(count));

        } else {
            Log.e("collectLetter", "No such marker in parsed markers array!");
        }
    }

    private void displayClosestLetters(Location location) {
        final String TAG = "displayClosestLetters";

        // Called every time location changes.
        if (location != null && map != null && mParsedMarkersRaw != null) {
            LatLng currentPos = new LatLng(location);

            // For iteration of mParsedMarkers array.
            int markerAtIndex = 0;
            for (MarkerViewOptions m : mParsedMarkersRaw) {
                // Have both type objects in place to add and remove from the map.
                Marker marker = mParsedMarkers.get(markerAtIndex);
                markerAtIndex++;

                // Distance for being able to grab a letter: 8 meters.
                if (currentPos.distanceTo(m.getPosition()) <= 8) {
                    if (mParsedMarkersRaw != null &&
                            !mLettersAround.contains(marker)) {
                        Log.d(TAG, "Spawning letter: " + m.getTitle()
                                + ", at distance of: " + currentPos.distanceTo(m.getPosition()));
                        mLettersAround.add(marker);
                        map.addMarker(m);

                        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
                    }
                } else {
                    if (mParsedMarkersRaw != null &&
                            mLettersAround.contains(marker)) {
                        Log.d(TAG, "Deleting letter: " + m.getTitle()
                                + ", at distance of: " + currentPos.distanceTo(m.getPosition()));
                        mLettersAround.remove(marker);
                        map.removeMarker(marker);
                    }
                }
            }
        }
    }

    private void prepareLightningMode() {
        // Check if the previously generated words were found and removed
        // or the game is run for the first time
        if (mLightningWords == null) {
            setRandomWords();
        }

        // Set up the required wordset as a Set<String> to pass onto SharedPreferences
        Set<String> wordset = new HashSet<>(Arrays.asList(mLightningWords));
        grabblePref.edit().putStringSet(LIGHT_REQUIRED, wordset).apply();

        // Set up the dialog for showing words and starting Lightning Mode.
        // An adapter is used to display words as lists of letters, just like
        // in the WordBagFragment.
        View dialogView = getLayoutInflater().inflate(R.layout.fragment_dialoglist, null);
        ListView dialogList = (ListView) dialogView.findViewById(R.id.dialog_word_list);
        dialogList.setAdapter(
                new LightningDialogViewAdapter(
                        MainActivity.this, R.layout.layout_dialoglistitem, mLightningWords)
        );
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);

        if (dialogList.getParent() != null) {
            ((ViewGroup) dialogList.getParent()).removeView(dialogList);
        }

        dialogBuilder.setView(dialogList);
        dialogBuilder.setTitle(R.string.lightning_title);
        dialogBuilder.setIcon(getDrawable(R.drawable.ic_timer_black_24dp));
        dialogBuilder.setNegativeButton(R.string.lightning_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setPositiveButton(R.string.lightning_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startLightningMode();
                dialog.dismiss();
            }
        });

        mLightningDialog = dialogBuilder.create();
        mLightningDialog.show();
    }

    private void setRandomWords() {
        int wordlistSize = sWordlist.size();
        int[] indices = new int[3];
        String[] words = new String[3];

        indices[0] = (int) (Math.random() * wordlistSize);
        indices[1] = (int) (Math.random() * wordlistSize);
        indices[2] = (int) (Math.random() * wordlistSize);

        for (int i = 0; i < 3; i++) {
            words[i] = sWordlist.get(indices[i]);
        }
        mLightningWords = words;
    }

    private void startLightningMode() {
        isLightningMode = true;

        // Show words that will need to be found
        TextView lightWord1 = (TextView) findViewById(R.id.light_word1);
        TextView lightWord2 = (TextView) findViewById(R.id.light_word2);
        TextView lightWord3 = (TextView) findViewById(R.id.light_word3);

        lightWord1.setText(mLightningWords[0].toUpperCase());
        lightWord2.setText(mLightningWords[1].toUpperCase());
        lightWord3.setText(mLightningWords[2].toUpperCase());

        findViewById(R.id.light_words).setVisibility(View.VISIBLE);

        // Set up the timer and design
        CountDownTimer ct = new CountDownTimer(900000, 1) {

            public void onTick(long mil) {
                String min = String.format(Locale.UK, "%02d", (int) mil/60000);
                String sec = String.format(Locale.UK, "%02d", (int) (mil%60000)/1000);
                String ms = String.format(Locale.UK, "%02d", (int) (mil % 1000)/10);

                mCountdown.setText(min + ":" + sec + ":" + ms);
                mCountdown.setVisibility(View.VISIBLE);

                mLightningButton.setClickable(false);
                mLightningButton.setBackgroundTintList(
                        ColorStateList.valueOf(getColor(R.color.grey_trans)));

                if (!isLightningMode) {
                    this.onFinish();
                }
            }

            public void onFinish() {
                mCountdown.setVisibility(View.INVISIBLE);

                mLightningButton.setClickable(true);
                mLightningButton.setBackgroundTintList(
                        ColorStateList.valueOf(getColor(R.color.accent)));

                if (lightningModeCompleted) {
                    // Give the user extra 200 points for successfully completing
                    // Lightning Mode.
                    int prevScore = grabblePref.getInt(HIGHSCORE, 0);
                    int newScore = prevScore + 200;

                    Log.d("btnGrabbleOnClick", "Lightning mode complete!!! Extra points: 200");
                    Log.d("btnGrabbleOnClick", "Current highscore!!! " + newScore);
                    grabblePref.edit().putInt(HIGHSCORE, newScore).apply();

                    Toast.makeText(
                            MainActivity.this,
                            (R.string.lightning_toast_completed),
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    // If the Mode wasn't completed, inform the user.
                    // Do not give any extras.
                    Toast.makeText(
                            MainActivity.this,
                            (R.string.lightning_toast_not_completed),
                            Toast.LENGTH_LONG)
                            .show();
                }

                findViewById(R.id.light_words).setVisibility(View.INVISIBLE);

                // Reset the fields for required and had Lightning words
                grabblePref.edit()
                        .remove(LIGHT_GOT)
                        .remove(LIGHT_REQUIRED)
                        .apply();

                // TODO: remove letters collected during lightning mode

                // Make a new set of random words
                setRandomWords();

                this.cancel();
            }
        };
        ct.start();
    }

    private class DaytimeChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {

            // Checking the current time and adjusting the map colours accordingly
            int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (currHour != dayHour && currHour == 8) {
                Log.d("DaytimeChangeReceiver", "Daytime changed, it's the dawn");
                map.setStyleUrl(getString(R.string.mapbox_mapref));
            } else if (currHour != dayHour && currHour == 18) {
                Log.d("DaytimeChangeReceiver", "Daytime changed, it's the sunset");
                map.setStyleUrl(getString(R.string.mapbox_mapref_night));
            }
            dayHour = currHour;
        }
    }

    private void setMapColours() {
        isNightAuto = settingsPref.getBoolean("nightmode_switch", true);
        if (mNavigationView != null)
            mNavigationView.getMenu().findItem(R.id.nav_nightmode).setVisible(!isNightAuto);

        if (isNightAuto && map != null) {
            int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (currHour >= 8 && currHour < 18) {
                map.setStyleUrl(getString(R.string.mapbox_mapref));
            } else {
                map.setStyleUrl(getString(R.string.mapbox_mapref_night));
            }
        } else if (!isNightAuto && map != null) {
            map.setStyleUrl(getString(R.string.mapbox_mapref));
        }
    }
}
