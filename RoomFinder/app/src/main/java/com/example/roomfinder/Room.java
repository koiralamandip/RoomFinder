package com.example.roomfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.internal.maps.zzt;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Room{
    private int room_id;
    private int room_count; // 1, 2, 3, 4...
    private String room_type; // Single, Flat, Single and Flat;
    private String locationName; // Place name
    private LatLng mapLocation; // Latitude and Longitude (for map plotting purpose only)
    private String ownerName;
    private String posterName;
    private String contact;
    private String price;

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    private int user_id;

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public int getRoom_count() {
        return room_count;
    }

    public void setRoom_count(int room_count) {
        this.room_count = room_count;
    }

    public String getRoom_type() {
        return room_type;
    }

    public void setRoom_type(String room_type) {
        this.room_type = room_type;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LatLng getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(LatLng mapLocation) {
        this.mapLocation = mapLocation;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;
    public static ArrayList<Room> roomList = new ArrayList<>();
    private ArrayList<String> imageNames = new ArrayList<>();
    private static final String roomFetchURL = "http://" + Session.serverIP + "/room_finder/rooms.php?REQ_FETCH";

    public Room(){

    }

    public Room(int room_id, int room_count, String room_type, String price, String locationName, LatLng mapLocation, String contact_primary, String ownerName, String posterName, int user_id, String description){
        this.room_id = room_id;
        this.room_count = room_count;
        this.room_type = room_type;
        this.locationName = locationName;
        this.price = price;
        this.mapLocation = mapLocation;
        this.contact = contact_primary;
        this.ownerName = ownerName;
        this.posterName = posterName;
        this.user_id = user_id;
        this.description = description;
    }

    public void setDetails(int room_id, int room_count, String room_type, String locationName, LatLng mapLocation, String ownerName, String contact, String posterName, String description, ArrayList<String> imageNames){
        this.room_id = room_id;
        this.room_count = room_count;
        this.room_type = room_type;
        this.locationName = locationName;
        this.mapLocation = mapLocation;
        this.ownerName = ownerName;
        this.contact = contact;
        this.description = description;
        this.imageNames = imageNames;
        this.posterName = posterName;
    }

    public static void addToList(Room room){
        roomList.add(room);
    }

    public void markOnMap(GoogleMap mMap){
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.position(this.mapLocation);
        mOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marker2));
        Marker marker = mMap.addMarker(mOptions);
        marker.setTag(this);
    }


    public static void fetchRooms(Context c){
        MapsActivity.pDialog.setMessage("Fetching Rooms...");
        MapsActivity.pDialog.show();

        final Context context = c;
        Room.roomList.clear();
        StringRequest getRequest = new StringRequest(Request.Method.GET, roomFetchURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // the response is already constructed as a JSONObject!
                MapsActivity.pDialog.dismiss();
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    if (status.equals("SUCCESS")){
                        JSONArray roomsArray = jsonResponse.getJSONArray("rooms");
                        for (int i = 0; i < roomsArray.length(); i++){
                            JSONObject room = roomsArray.getJSONObject(i);
                            LatLng loc = new LatLng(room.getDouble("latitude"), room.getDouble("longitude"));
                            Room roomObj = new Room(room.getInt("room_id"), room.getInt("room_count"), room.getString("room_type"), room.getString("price"), room.getString("location"), loc, room.getString("contact"), room.getString("owner"), room.getString("poster"), room.getInt("user_id"), room.getString("description"));
                            roomObj.markOnMap(MapsActivity.mMap);
                            if (roomObj.getUser_id() == Session.user_id)
                                addToList(roomObj);
                        }
                    }else{
                        Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        Volley.newRequestQueue(context).add(getRequest);
    }

    public String[] getArrayofData(){
        String[] array = {
                String.valueOf(room_id),
                String.valueOf(room_count),
                room_type,
                locationName,
                String.valueOf(mapLocation.latitude),
                String.valueOf(mapLocation.longitude),
                ownerName,
                posterName,
                contact,
                String.valueOf(user_id),
                description,
                price
        };
        return array;
    }
}
