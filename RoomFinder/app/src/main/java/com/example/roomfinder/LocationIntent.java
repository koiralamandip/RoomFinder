package com.example.roomfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationIntent extends AppCompatActivity implements OnMapReadyCallback {

    private LatLng mapLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_intent);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        mapLocation = new LatLng(i.getDoubleExtra("latitude",27.45), i.getDoubleExtra("longitude",285.45));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final GoogleMap gMap = googleMap;
        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
        gMap.addMarker(new MarkerOptions().position(mapLocation));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, 12));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                gMap.clear();
                mapLocation = latLng;
                Toast.makeText(getApplicationContext(), String.valueOf(mapLocation.latitude), Toast.LENGTH_SHORT).show();
                gMap.addMarker(new MarkerOptions().position(mapLocation));
                gMap.animateCamera(CameraUpdateFactory.newLatLng(mapLocation));
            }
        });
    }

    public void okMap(View view){
        Intent i = new Intent();
        i.putExtra("latitude", mapLocation.latitude);
        i.putExtra("longitude", mapLocation.longitude);
        setResult(RESULT_OK, i);
        finish();

    }
}
