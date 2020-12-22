package com.example.roomfinder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.example.roomfinder.MapsActivity.currentLocation;
import static com.example.roomfinder.MapsActivity.locationManager;
import static com.example.roomfinder.MapsActivity.m;
import static com.example.roomfinder.MapsActivity.mMap;

public class RoomActivity extends AppCompatActivity implements LocationListener {
    private String type;
    private String[] roomData;
    private LatLng mapLocation;
    private String room_id;
    private final int REQ_LOCATION = 10;
    private final String addRoomURL = "http://" + Session.serverIP + "/room_finder/rooms.php?REQ_ADD";
    private final String updateRoomURL = "http://" + Session.serverIP + "/room_finder/rooms.php?REQ_EDIT";

    public static String provider;
    public static LocationManager locationManager;
    private Location l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        roomData = intent.getStringArrayExtra("roomData");
        room_id = roomData[0];
        mapLocation = new LatLng(Double.parseDouble(roomData[4]), Double.parseDouble(roomData[5]));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (type.equals("Add")){
            ((Button) findViewById(R.id.roomDBBtn)).setText("Add Room");
            ((TextView) findViewById(R.id.roomDBText)).setText("Add New Room");
            roomInit();
        }
        else{
            ((TextView) findViewById(R.id.roomDBText)).setText("Edit Room");
            ((Button) findViewById(R.id.roomDBBtn)).setText("Edit Room");
            roomInit();
        }
    }

    private void roomInit(){
        ((EditText) findViewById(R.id.room_count)).setText(roomData[1]);
        ((EditText) findViewById(R.id.room_type)).setText(roomData[2]);
        ((EditText) findViewById(R.id.location)).setText(roomData[3]);
        ((EditText) findViewById(R.id.ownerName)).setText(roomData[6]);
        ((EditText) findViewById(R.id.description)).setText(roomData[10]);
        ((EditText) findViewById(R.id.contact)).setText(roomData[8]);
        ((EditText) findViewById(R.id.price)).setText(roomData[11]);
    }


    public void getMapLocation(View view){
        Intent i = new Intent(getApplicationContext(), LocationIntent.class);
        i.putExtra("latitude", mapLocation.latitude);
        i.putExtra("longitude", mapLocation.longitude);
        startActivityForResult(i, REQ_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == REQ_LOCATION){
                mapLocation = new LatLng(data.getExtras().getDouble("latitude"), data.getExtras().getDouble("longitude"));
            }
        }

    }

    public void roomDB(View view){
        if (type.equals("Add")){
            addRoom();
        }else{
            updateRoom();
        }
    }

    private void addRoom(){
        final String room_count = ((EditText) findViewById(R.id.room_count)).getText().toString();
        final String room_type = ((EditText) findViewById(R.id.room_type)).getText().toString();
        final String location = ((EditText) findViewById(R.id.location)).getText().toString();
        final String latitude = String.valueOf(mapLocation.latitude);
        final String longitude = String.valueOf(mapLocation.longitude);
        final String contact = ((EditText) findViewById(R.id.contact)).getText().toString();
        final String ownerName = ((EditText) findViewById(R.id.ownerName)).getText().toString();
        final String description = ((EditText) findViewById(R.id.description)).getText().toString();
        final String price = ((EditText) findViewById(R.id.price)).getText().toString();

            StringRequest postRequest = new StringRequest(Request.Method.POST, addRoomURL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // the response is already constructed as a JSONObject!
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("RES", "**********************" + response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("SUCCESS")){
                            Toast.makeText(getApplicationContext(), "Room added", Toast.LENGTH_SHORT).show();
                            MapsActivity.reFetchRooms(getApplicationContext());
                            finish();
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
                    params.put("room_count", room_count);
                    params.put("room_type", room_type);
                    params.put("location", location);
                    params.put("latitude", latitude);
                    params.put("longitude", longitude);
                    params.put("contact", contact);
                    params.put("ownerName", ownerName);
                    params.put("description", description);
                    params.put("price", price);
                    params.put("user_id", String.valueOf(Session.user_id));
                    return params;
                }
            };

        if (room_count.equals("") || room_id.equals("") || room_type.equals("") || location.equals("") || price.equals("") || latitude.equals("") || longitude.equals("") || contact.equals("") || ownerName.equals("") || description.equals("")){
            Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        }else {
            Volley.newRequestQueue(this).add(postRequest);
        }
//        }catch(Exception e){

//        }
    }

    private void updateRoom(){
        final String room_count = ((EditText) findViewById(R.id.room_count)).getText().toString();
        final String room_type = ((EditText) findViewById(R.id.room_type)).getText().toString();
        final String location = ((EditText) findViewById(R.id.location)).getText().toString();
        final String latitude = String.valueOf(mapLocation.latitude);
        final String longitude = String.valueOf(mapLocation.longitude);
        final String contact = ((EditText) findViewById(R.id.contact)).getText().toString();
        final String ownerName = ((EditText) findViewById(R.id.ownerName)).getText().toString();
        final String description = ((EditText) findViewById(R.id.description)).getText().toString();
        final String price = ((EditText) findViewById(R.id.price)).getText().toString();

//        try{
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            if (bitmap != null){
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//            }
//            final String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);


        StringRequest postRequest = new StringRequest(Request.Method.POST, updateRoomURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // the response is already constructed as a JSONObject!
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    if (status.equals("SUCCESS")){
                        Toast.makeText(getApplicationContext(), "Room updated", Toast.LENGTH_SHORT).show();
                        MapsActivity.reFetchRooms(getApplicationContext());
                        finish();
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
                params.put("room_count", room_count);
                params.put("room_id", room_id);
                params.put("room_type", room_type);
                params.put("location", location);
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("contact", contact);
                params.put("ownerName", ownerName);
                params.put("description", description);
                params.put("price", price);
                params.put("user_id", String.valueOf(Session.user_id));
                return params;
            }
        };

        if (room_count.equals("") || room_id.equals("") || room_type.equals("") || location.equals("") || price.equals("") || latitude.equals("") || longitude.equals("") || contact.equals("") || ownerName.equals("") || description.equals("")){
            Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        }else{
            Volley.newRequestQueue(this).add(postRequest);
        }

//        }catch(Exception e){

//        }
    }

    public void getGPSLocation(View view){
        getUpdate();
    }

    private boolean checkLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int result2 = getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            return result == PackageManager.PERMISSION_GRANTED || result2 == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

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

    public static boolean isLocationEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void getUpdate(){
        if (checkLocationPermission()){
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
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                provider = locationManager.getBestProvider(criteria, true);
                try{
                    l = locationManager.getLastKnownLocation(provider);
                    if (!l.equals(null)){
                        LatLng ln = new LatLng(l.getLatitude(), l.getLongitude());
                        mapLocation = ln;
                    }
                }catch(Exception e){

                }
                locationManager.requestLocationUpdates(provider, 1000 * 60, 0.5f, this);
            }
        }else{
            Toast.makeText(getApplicationContext(),"Please click the button once again after allowing permission", Toast.LENGTH_LONG).show();
            requestPermissionLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mapLocation = currentLocation;
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
