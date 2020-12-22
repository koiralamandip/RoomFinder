package com.example.roomfinder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.test.mock.MockPackageManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.GlideContext;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/***
 * The default activity that loads on start of the application
 * Implements LocationListener to get the current location of the user
 * Map is added in this activity to show all available rooms to users
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    // Google Map used in this activity
    public static GoogleMap mMap;

    // Server pages URLs for login and registration
    private final String loginURL = "http://" + Session.serverIP + "/room_finder/users.php?REQ_LOGIN";
    private final String regURL = "http://" + Session.serverIP + "/room_finder/users.php?REQ_JOIN";

    // Bitmap to store images from the camera or gallery.
    private Bitmap bitmap;

    // Intent REQUEST CODES
    final private int REQ_CAMERA = 100;
    final private int REQ_GALLERY = 200;

    // ImageView to show profile pciture of logged in user
    private ImageView profileView;

    // TO show progress dialog while the rooms will be fetched from the server
    public static ProgressDialog pDialog;

    //SQLite database and cursor to point on fetched rows.
    public SQLiteDatabase sqliteDB;
    public Cursor cursorDB;

    // The name of best provider selected based on criteria
    public static String provider;

    public static LocationManager locationManager;

    // To store the current location of a user
    public static LatLng currentLocation = null;

    // Marker of current location in map. Declared static for changing the marker's position frequently from anywhere if needed.
    public static Marker m = null;

    private Location l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Request Runtime Permission for location usage if not already granted
        initLocationPermissionSeeker();

        // Location Manager gets the location service from the system
        locationManager =(LocationManager) getSystemService(LOCATION_SERVICE);
        // Method to fetch the user's current location and show on map. Calling it here means the location of a user is shown on map when the application is loaded
        getUpdate();

        // ImageView to store profile picture
        profileView = (ImageView) findViewById(R.id.profile);

        ((Button) findViewById(R.id.loginCancelBtn)).setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "icomoon.ttf"));
        ((Button) findViewById(R.id.regCancelBtn)).setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "icomoon.ttf"));

        OptionButton regLoad = (OptionButton) findViewById(R.id.regLoad);
        regLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrationLoader();
            }
        });

        OptionButton loginLoad = (OptionButton) findViewById(R.id.loginLoad);
        loginLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginLoader();
            }
        });

        OptionButton gpsStarter = (OptionButton) findViewById(R.id.gpsBtn);
        gpsStarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        // SQLite Database related
        File file = new File(getFilesDir() + "/roomfinderdb.db");
        SqliteConnection sqliteConn = new SqliteConnection(file);
        sqliteDB = sqliteConn.getDatabaseObject();

        pDialog = new ProgressDialog(MapsActivity.this);
        pDialog.setCanceledOnTouchOutside(false);

        /**
         *  If a session is already available when this activity is loaded, then the user of the session is logged in.
         */
        if (Session.isAvailable){
            changesWhileLoggedIn();
        }else{
            // If session is not available and session is recently logged out, then show the logged-out like screen, i.e. remove logged-in features
            if (Session.isLoggedOut){
                changesWhileLoggedOut();
                Session.isLoggedOut = false;
            }else{
                // If the session is not available and also there was no recent logout event, then there might be previous session in SQLite database, which is read here.
                // If there is any user in SQLite database, log-in that user, else start a fresh activity.

                cursorDB = sqliteDB.rawQuery("select * from users", null);
                // If there is a user found, get the details, check password and allow access to the application
                if (cursorDB.getCount() > 0){
                    //Move the cursor to the first element of fetched data and process further
                    if (cursorDB.moveToFirst()) {
                        String user_id = cursorDB.getString((cursorDB.getColumnIndex("user_id"))).toString();
                        String firstname = cursorDB.getString(cursorDB.getColumnIndex("firstname")).toString();
                        String surname= cursorDB.getString(cursorDB.getColumnIndex("surname")).toString();
                        String username = cursorDB.getString(cursorDB.getColumnIndex("username")).toString();
                        String phone = cursorDB.getString(cursorDB.getColumnIndex("phone")).toString();
                        // Set the session of the user fetched from SQLite database
                        Session.setSession(Integer.parseInt(user_id), firstname, surname, username, phone);
                        // Make logged-in changes to the screen
                        changesWhileLoggedIn();
                    }
                }else{
                    changesWhileLoggedOut();
                }
                cursorDB.close();
            }
        }
        // Method used to fetch room details from the remote database
        Room.fetchRooms(getApplicationContext());
    }

    public static void reFetchRooms(Context c){
        mMap.clear();
        Room.fetchRooms(c);
    }

    // Show "My Rooms", "Add Room", "Profile Pciture" and "Log Out" buttons when a user logs in
    private void changesWhileLoggedIn(){
        ((LinearLayout) findViewById(R.id.loggedInInfo)).setVisibility(View.VISIBLE);
        ((LinearLayout) findViewById(R.id.notLoggedInInfo)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.topMenu)).setVisibility(View.VISIBLE);
        // URL of the profile picture of logged-in user
        String url = "http://" + Session.serverIP + "/room_finder/images/profiles/" + Session.username + Session.user_id + ".jpg?unique=" + new Timestamp(new Date().getTime());

        final ImageView v = ((ImageView) findViewById(R.id.loggedInImage));

        // Loading image from database using Glide API into the imageview
        Glide.with(getApplicationContext())
                .load(url)
                .into(v);

        ((RelativeLayout) findViewById(R.id.divRegistration)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.divLogin)).setVisibility(View.GONE);
    }

    // Hide "My Rooms", "Add Room", "Logout" buttons when logged-out
    private void changesWhileLoggedOut(){
        ((LinearLayout) findViewById(R.id.topMenu)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.loggedInInfo)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.notLoggedInInfo)).setVisibility(View.VISIBLE);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));

        // Move the camera to show the inserted location by default.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(27.7668, 85.4066),12));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Actionlistener to show detials of room when clicked on repsective markers on map.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // If the clicked marker is of CURRENT LOCATION, do nothing and return
                if (marker.equals(m))
                    return false;
                // else, get the tag (ROOM object) associated with the clicked marker
                Session.room = (Room) marker.getTag();
                // Open HomeActivity where the details of the selected room will be shown
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                // View room is sent to indicate that any other user needs to view the details of room poster by another poster
                // Passing "my_room" in HomeActivity will show all the rooms posted by the logged-in user
                intent.putExtra("type", "view_room");
                startActivity(intent);
                return false;
            }
        });
    }

    // On click listener of the of the logout button
    public void logoutAction(View view){
        Session.isAvailable = false;
        Session.isLoggedOut = true;
        Cursor result = sqliteDB.rawQuery("delete from users", null);
        if (result.getCount() == 0)
            Toast.makeText(getApplicationContext(), "User logged out", Toast.LENGTH_SHORT).show();
        result.close();
        changesWhileLoggedOut();
    }

    public void loginCanceller(View view){
        RelativeLayout loginDiv = (RelativeLayout) findViewById(R.id.divLogin);
        if (loginDiv.getVisibility() == View.VISIBLE)
            loginDiv.setVisibility(View.GONE);

        ((EditText) findViewById(R.id.username)).setText("");
        ((EditText) findViewById(R.id.password)).setText("");
    }

    public void registrationCanceller(View view){
        RelativeLayout registrationDiv = (RelativeLayout) findViewById(R.id.divRegistration);
        if (registrationDiv.getVisibility() == View.VISIBLE)
            registrationDiv.setVisibility(View.GONE);

        ((EditText) findViewById(R.id.regFirstname)).setText("");
        ((EditText) findViewById(R.id.regSurname)).setText("");
        ((EditText) findViewById(R.id.regPhone)).setText("");
        ((EditText) findViewById(R.id.regUsername)).setText("");
        ((EditText) findViewById(R.id.regPassword)).setText("");
        ((EditText) findViewById(R.id.regPasswordRe)).setText("");
    }

    // The click listener of button which shows registration form
    public  void registrationLoader(){
        RelativeLayout registrationDiv = (RelativeLayout) findViewById(R.id.divRegistration);
        RelativeLayout loginDiv = (RelativeLayout) findViewById(R.id.divLogin);
        if (loginDiv.getVisibility() == View.VISIBLE)
            loginDiv.setVisibility(View.GONE);
        if (registrationDiv.getVisibility() != View.VISIBLE)
            registrationDiv.setVisibility(View.VISIBLE);
    }

    // The click listener of button which shows login form
    public  void loginLoader(){
        RelativeLayout loginDiv = (RelativeLayout) findViewById(R.id.divLogin);
        RelativeLayout registrationDiv = (RelativeLayout) findViewById(R.id.divRegistration);
        if (registrationDiv.getVisibility() == View.VISIBLE)
            registrationDiv.setVisibility(View.GONE);
        if (loginDiv.getVisibility() != View.VISIBLE)
            loginDiv.setVisibility(View.VISIBLE);
    }

    // When "Login" button is clicked
    public void loginAction(View view){
        final String username = ((EditText) findViewById(R.id.username)).getText().toString();
        final String password = ((EditText) findViewById(R.id.password)).getText().toString();

        // Set up a Volley String request with POST method and pass login parameters
        StringRequest postRequest = new StringRequest(Request.Method.POST, loginURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // the response is already constructed as a JSONObject!
                try {
                    // Parse JSONObject and retrieve response from the server
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    if (status.equals("SUCCESS")){
                        JSONObject userDataObject = jsonResponse.getJSONObject("data");
                        // Get the details of logged in user given as a response by the server, and create a session with the details
                        Session.setSession(userDataObject.getInt("user_id"), userDataObject.getString("firstname"), userDataObject.getString("surname"),userDataObject.getString("username"),userDataObject.getString("phone"));
                        // Store currently logged in user in SQLite for checking session when the application is loaded the next time
                        Cursor result = sqliteDB.rawQuery("insert into users values (?, ?, ?, ?, ?)", new String[] {userDataObject.getString("user_id"), userDataObject.getString("firstname"), userDataObject.getString("surname"),userDataObject.getString("username"),userDataObject.getString("phone")});
                        if (result.getCount() == 0)
                            Toast.makeText(getApplicationContext(), "User " + username + " logged in", Toast.LENGTH_SHORT).show();
                        result.close();
                        // Refetch room details from the database once the user logs in
                        reFetchRooms(MapsActivity.this);
                        changesWhileLoggedIn();
                    }else{
                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        if (username.equals("")){
            Toast.makeText(getApplicationContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
        }else if (password.equals("")){
            Toast.makeText(getApplicationContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
        }else{
            Volley.newRequestQueue(this).add(postRequest);
        }
    }

    // Volley registration request to the remote server
    public void registerAction(View view){
        final String firstname = ((EditText) findViewById(R.id.regFirstname)).getText().toString();
        final String surname = ((EditText) findViewById(R.id.regSurname)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.regPhone)).getText().toString();
        final String username = ((EditText) findViewById(R.id.regUsername)).getText().toString();
        final String password = ((EditText) findViewById(R.id.regPassword)).getText().toString();
        final String rePassword = ((EditText) findViewById(R.id.regPasswordRe)).getText().toString();
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (bitmap != null){
                // compress the image file
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            }
            // Encode the image file using Base64 into string format
            final String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            StringRequest postRequest = new StringRequest(Request.Method.POST, regURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // the response is already constructed as a JSONObject!
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("SUCCESS")){
                            Toast.makeText(getApplicationContext(), "User " + username + " registered", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error instanceof NoConnectionError)
                    {
                        Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
                        // Did not connect
                    }
                    else if(error instanceof TimeoutError)
                    {
                        Toast.makeText(getApplicationContext(), "Timeout", Toast.LENGTH_SHORT).show();
                        //Response Timeout error.
                    }
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    // the POST parameters:
                    params.put("firstname", firstname);
                    params.put("surname", surname);
                    params.put("phone", phone);
                    params.put("username", username);
                    params.put("password", password);
                    params.put("image", encodedImage);
                    return params;
                }
            };
            if (firstname.equals("") || surname.equals("")){
                Toast.makeText(getApplicationContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (phone.equals("")){
                Toast.makeText(getApplicationContext(), "Phone cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (username.equals("") || password.equals("")){
                Toast.makeText(getApplicationContext(), "Username and Password cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (!password.equals(rePassword)){
                Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            }else if (encodedImage.equals("") || bitmap == null){
                Toast.makeText(getApplicationContext(), "Please select a profile picture", Toast.LENGTH_SHORT).show();
            }else{
                Volley.newRequestQueue(this).add(postRequest);
            }
        }catch(Exception e){

        }

    }

    // Shows the alert dialog to select between camera and gallery for profile picture
    public void setProfileLoader(View view){
        CharSequence colors[] = new CharSequence[]{"Select From Gallery", "Capture From Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Select Option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        // If gallery is selected, open gallery intent
                        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");
                        startActivityForResult(pickIntent, REQ_GALLERY);
                        break;
                    case 1:
                        // If camera is selected, open camera intent
                        Intent camInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camInt, REQ_CAMERA);
                        break;
                }
            }
        });
        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If result is returned by camera intent
        if (requestCode == REQ_CAMERA){
            bitmap = (Bitmap) data.getExtras().get("data");
            profileView.setImageBitmap(bitmap);
        }

        // If result is returned by gallery intent
        if (requestCode == REQ_GALLERY){
            try{
                Uri selectedImage = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                profileView.setImageBitmap(bitmap);
            }catch(Exception e){

            }
        }
    }

    // Onclick listener of "Add Room" button
    public void addRoomAction(View view){
        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
        intent.putExtra("type", "Add");
        Room room = new Room();
        room.setMapLocation(new LatLng(27.7668,85.4066));

        intent.putExtra("roomData", room.getArrayofData());
        startActivity(intent);
    }

    // Onclick listener of "My Rooms" button
    public void homeLoader(View view){
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("type", "my_room");
        startActivity(intent);
    }

    // GPS button on click listener
    public void getLocation(){
        getUpdate();
    }

    // Checking permission for GPS or Network location usage
    private boolean checkLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int result2 = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            return result == PackageManager.PERMISSION_GRANTED || result2 == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    // Requesting runtime permission of location to user
    public void requestPermissionLocation(){
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    10);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void initLocationPermissionSeeker(){
        if (!checkLocationPermission()){
            requestPermissionLocation();
        }
    }

    // To see if location providers are enabled to access location or not
    public static boolean isLocationEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // Fetch current location of user
    public void getUpdate(){
        // Check for location permission
        if (checkLocationPermission()){
            // If location settings are not enabled, ask user to enable them
            if (!isLocationEnabled()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Usage");
                builder.setMessage("Please enable location settings for us to fetch your location!");
                builder.setPositiveButton("Enable Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }else{
                // Select best provider based on criteria
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                provider = locationManager.getBestProvider(criteria, true);
                try{
                    // If getLastKnownLocation() has existing location to return then that location will be first returned to the user for faster response
                    l = locationManager.getLastKnownLocation(provider);
                    if (!l.equals(null)){
                        if (m != null) m.remove();
                        LatLng ln = new LatLng(l.getLatitude(), l.getLongitude());
                        m = mMap.addMarker(new MarkerOptions().position(ln));
                        m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_current6));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ln, 20));
                    }
                }catch(Exception e){

                }
                // Updating location continuously after 1 mniute and/or 0.5 meters distance and get the location using this locationlistener
                locationManager.requestLocationUpdates(provider, 1000 * 60, 0.5f, this);
            }
        }else{
            requestPermissionLocation();
        }
    }

    // Called when location change is detected
    @Override
    public void onLocationChanged(Location location) {
        // Get the changed location
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (m != null) m.remove();
        // Change the location of the CURRENT LOCATION marker
        locationManager.removeUpdates(this);
        m = mMap.addMarker(new MarkerOptions().position(currentLocation));
        // Set custom icon for the marker
        m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_current6));
        // animate the camera to move from previous point to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
