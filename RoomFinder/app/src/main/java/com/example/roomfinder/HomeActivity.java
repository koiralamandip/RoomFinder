package com.example.roomfinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

public class HomeActivity extends AppCompatActivity{
    private LinearLayout holder;
    private final String roomFetchURL = "http://" + Session.serverIP + "/room_finder/rooms.php?REQ_FETCH";
    private final String deleteRoomURL = "http://" + Session.serverIP + "/room_finder/rooms.php?REQ_DELETE";
    String data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        holder = (LinearLayout) findViewById(R.id.blockHolder);
        Intent i = getIntent();
        data  = i.getStringExtra("type");

        if (data.equals("my_room")){
            addBlocks();
        }else if (data.equals("view_room")){
            ((TextView) findViewById(R.id.titleHome)).setText("Room details");
            addBlock();
        }
    }

    private void addBlock(){
        if (Session.room != null){
            inflateAndShow(Session.room);
            Session.room = null;
        }
    }

    private void addBlocks(){
        for (Room room : Room.roomList){
            inflateAndShow(room);
        }
    }

    private void inflateAndShow(Room room){
        final Room r = room;
        View msgBox = (View) getLayoutInflater().inflate( R.layout.room_block, holder,false);
        TextView room_count = (TextView) msgBox.findViewById(R.id.room_count);
        TextView room_type = (TextView) msgBox.findViewById(R.id.room_type);
        TextView location = (TextView) msgBox.findViewById(R.id.location);
        TextView contact = (TextView) msgBox.findViewById(R.id.contact);
        TextView ownerName = (TextView) msgBox.findViewById(R.id.ownerName);
        TextView price = (TextView) msgBox.findViewById(R.id.price);
        TextView description = (TextView) msgBox.findViewById(R.id.description);
        TextView poster = (TextView) msgBox.findViewById(R.id.poster);
        TextView imageInfo = (TextView) msgBox.findViewById(R.id.infoImage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageInfo.setText(Html.fromHtml("<b><font color='blue'>Update Info</font></b>: <p>We will be pushing an update soon with additional features</p><p>1. Images for rooms can now be posted</p>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            imageInfo.setText(Html.fromHtml("<b><font color='blue'>Update Info</font></b>: <p>We will be pushing an update soon with additional features</p><p>1. Images for rooms can now be posted</p>"));
        }

        room_count.setText(String.valueOf(room.getRoom_count()));
        room_type.setText(room.getRoom_type());
        location.setText(room.getLocationName());
        contact.setText(room.getContact());
        ownerName.setText(room.getOwnerName());
        description.setText(room.getDescription());
        price.setText(room.getPrice());

        OptionButton shareBtn = (OptionButton) msgBox.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMultiple = new Intent(Intent.ACTION_SEND);
                intentMultiple.setType("text/plain");
                String value = "Hy there, I have shared with you the details of a room as below.\n\n";
                value += "1. Number of rooms available: " + r.getRoom_count() + "\n";
                value += "2. Type of room: " + r.getRoom_type() + "\n";
                value += "3. Price (per Single, per Flat): " + r.getPrice() + "\n";
                value += "4. Location: " + r.getLocationName() + "\n";
                value += "5. Contact number: " + r.getContact() + "\n";
                value += "6. Owner Name: " + r.getOwnerName() + "\n";
                value += "7. Posted (on Room Finder App) by: " + r.getPosterName() + "\n\n";
                value += r.getDescription() + "\n\n";
                value += "See on Maps: \n\nhttps://www.google.com/maps/search/?api=1&query=" + r.getMapLocation().latitude + "," + r.getMapLocation().longitude;

                intentMultiple.putExtra(Intent.EXTRA_TEXT, value);
                intentMultiple.putExtra(Intent.EXTRA_SUBJECT, "Room details (From Room Finder App)");
                startActivity(Intent.createChooser(intentMultiple, "Share this room via"));

            }
        });

        if (data.equals("my_room")){
            ((LinearLayout) msgBox.findViewById(R.id.posterLayout)).setVisibility(GONE);
            OptionButton viewLocBtn = (OptionButton) msgBox.findViewById(R.id.locatioView);
            OptionButton callButton = (OptionButton) msgBox.findViewById(R.id.callButton);
            callButton.setVisibility(GONE);
            OptionButton smsButton = (OptionButton) msgBox.findViewById(R.id.smsButton);
            smsButton.setVisibility(GONE);

            viewLocBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), LocationIntent.class);
                    i.putExtra("latitude", r.getMapLocation().latitude);
                    i.putExtra("longitude", r.getMapLocation().longitude);
                    startActivity(i);
                }
            });

            OptionButton editRoom = (OptionButton) msgBox.findViewById(R.id.editRoom);
            editRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                    intent.putExtra("type", "Edit");
                    intent.putExtra("roomData", r.getArrayofData());
                    startActivity(intent);
                }
            });

            OptionButton deleteRoom = (OptionButton) msgBox.findViewById(R.id.deleteRoom);
            deleteRoom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setTitle("Delete Warning");
                    builder.setMessage("Do you really want to delete the selected entry?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteRoomAction(r.getRoom_id());
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }else if (data.equals("view_room")){
            poster.setText(room.getPosterName());
            ((OptionButton) msgBox.findViewById(R.id.deleteRoom)).setVisibility(GONE);
            ((OptionButton) msgBox.findViewById(R.id.editRoom)).setVisibility(GONE);
            ((OptionButton) msgBox.findViewById(R.id.locatioView)).setVisibility(GONE);
            OptionButton callButton = (OptionButton) msgBox.findViewById(R.id.callButton);
            callButton.setVisibility(View.VISIBLE);
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + r.getContact()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            OptionButton smsButton = (OptionButton) msgBox.findViewById(R.id.smsButton);
            smsButton.setVisibility(View.VISIBLE);
            smsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                    sendIntent.setData(Uri.parse("sms:" + r.getContact()));
                    startActivity(sendIntent);
                }
            });
        }

        holder.addView(msgBox,-1);
    }

    private void deleteRoomAction(int room_id){
        final int r = room_id;

        StringRequest postRequest = new StringRequest(Request.Method.POST, deleteRoomURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // the response is already constructed as a JSONObject!
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    if (status.equals("SUCCESS")){
                        Toast.makeText(getApplicationContext(), "Room deleted", Toast.LENGTH_SHORT).show();
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
                params.put("room_id", String.valueOf(r));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(postRequest);
    }
}
