package com.example.hussain.mapmylocation;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public  class MainActivity extends AppCompatActivity  {
    LocationManager locationManager;
    MapView map;
    Location location;
    Location lastLocation = null;
    LocationListener locationListener;
    IMapController mapcontrol;
    Criteria criteria;
    NetworkChangeReceiver networkChangeReceiver;
    IntentFilter networkChangeReceiverFilter;
    FloatingActionButton fab;
    ProgressDialog progressDialog;
    SharedPreferences preferences;
    Road road;
    ArrayList<GeoPoint> waypoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getApplicationContext().getSharedPreferences("",Context.MODE_PRIVATE);
        //Initializing map
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        //getting location
        criteria = new Criteria();
        locationListener = new MyLocationListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //handling change of location provider
        networkChangeReceiverFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        //Setting up UI and map
        setupUIElements();
        setupMap();
    }

    /**
     * Called when the app is started and put in memory,
     * previously killed by the user or system for memory
     */
    @Override
    protected void onStart() {
        super.onStart();
        setupLocationUpdates();
    }
    private BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Checking for GPS");
            progressDialog.setMessage("Please wait");
            progressDialog.dismiss();
        }
    };

    /**
     * Location change is sensed and handled here
     *
     * @param location- location data
     */

    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        updateLoc(location);
    }


    /**
     * Shows an alert dialog to enable GPS.
     *
     * @param provider - Location provider (Either GPS or Network)
     */

    public void requestGPS(String provider) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "GPS needs to be turned on";

        builder.setMessage(message)
                .setPositiveButton("Turn On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    /**
     * Switches to the best available location provider. Called by the Location listener
     *
     * @param provider - Location provider (Either GPS or Network)
     */

    public void switchProvider(String provider) {
        setupLocationUpdates();
    }

    /**
     * Function to display route between
     * two goepoints provided by the server
     * @param p1-Pickup Geolocation
     * @param p2-Drop Geolocation
     */
    public void drawRoute(final GeoPoint p1,final  GeoPoint p2) {
        new Thread(new Runnable() {
            public void run() {
                RoadManager roadManager = new OSRMRoadManager(MainActivity.this);
                waypoints = new ArrayList<GeoPoint>();
                waypoints.add(p1);
                waypoints.add(p2);

                try {
                    road = roadManager.getRoad(waypoints);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*
                Drawing route on a thread seprate from main
                thread  to improve performance
                 */
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (road.mStatus != Road.STATUS_OK) {
                            Toast.makeText(getApplicationContext(), "Error when loading the road - status=" + road.mStatus, Toast.LENGTH_SHORT).show();
                        }

                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 8);
                        map.getOverlays().add(roadOverlay);
                        FolderOverlay roadMarkers = new FolderOverlay();
                        map.getOverlays().add(roadMarkers);
                    }
                });
            }
        }).start();

    }


    /**
     * Updates the marker position on the map to the given location
     *
     * @param loc - Location lat and lng
     */
    private void updateLoc(final Location loc) {
        if (loc == null) {
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Checking for GPS");
            progressDialog.setMessage("Please wait");
            progressDialog.show();
            Log.e("Null Location","@updateLoc");
            return;
        }
        lastLocation = loc;
        map.getOverlays().clear();
        GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        if(progressDialog!=null)
            progressDialog.dismiss();
        map.getController().animateTo(point);
        Double la=Double.parseDouble(preferences.getString("loc_la", "0.0"));
        Double lo=Double.parseDouble(preferences.getString("loc_lo", "0.0"));
        Log.e("pos",la+" "+lo);
        Log.e("pos2",loc.getLatitude()+" "+loc.getLongitude());
        GeoPoint p1=new GeoPoint(la,lo);
        GeoPoint p2=new GeoPoint(loc.getLatitude(),loc.getLongitude());
        drawRoute(p1,p2);

        Toast.makeText(getApplicationContext(),loc.getLatitude()+" "+loc.getLongitude(), Toast.LENGTH_SHORT).show();

        Marker startMarker = new Marker(map);
        startMarker.setPosition(point);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(getResources().getDrawable(R.drawable.ic_action_name));
        map.getOverlays().add(startMarker);
        map.invalidate();
    }


    /**
     * Initialises the map and sets up required parameters
     */
    public void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setTilesScaledToDpi(true);
        mapcontrol = map.getController();
        mapcontrol.setZoom(16);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
    }


    /**
     * Sets up the UI Elements like FAB onClick listeners, and
     * Navigation Drawer actions.
     */
    private void setupUIElements() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastLocation == null||location==null) {
                    location=lastLocation;
                    progressDialog=new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Checking for GPS");
                    progressDialog.setMessage("Please wait");
                    progressDialog.show();
                    return;
                }
                if(progressDialog!=null)
                    progressDialog.dismiss();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("loc_la",String.valueOf(lastLocation.getLatitude()));
                editor.putString("loc_lo",String.valueOf(lastLocation.getLongitude()));
                editor.commit();
                GeoPoint point = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                map.getController().animateTo(point);
                Marker startMarker = new Marker(map);
                startMarker.setPosition(point);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setIcon(getResources().getDrawable(R.drawable.ic_action_name));
                map.getOverlays().add(startMarker);

            }
        });


    }


    /**
     * Picks the best Location Provider that is ENABLED.
     * Registers listener for location updates
     */
    private void setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(bestProvider, 1000, 100, locationListener);
        location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            updateLoc(location);
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Unable to get Location");
            alert.setMessage("Check if GPS is switched on or move outside building");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface alert, int which) {
                    alert.dismiss();
                }
            });
            alert.show();
        }
    }
}