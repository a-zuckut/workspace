package com.example.codewars.app;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.kaazing.net.ws.WebSocket;
import org.kaazing.net.ws.WebSocketFactory;
import org.kaazing.net.ws.WebSocketMessageReader;
import org.kaazing.net.ws.WebSocketMessageType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShowData extends ActionBarActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private final Map<String, MarkerOptions> map = new ConcurrentHashMap<String, MarkerOptions>();
    private GoogleMap googleMap;
    private TextView longitude;
    private TextView latitude;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;
    public String stringForGateway;
    private ListView listView;
    private ArrayList<String> namesForListView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        Log.i("Name", getIntent().getStringExtra("name"));

        namesForListView = new ArrayList<String>();
        listView = (ListView)findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, namesForListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String x = (String)listView.getItemAtPosition(position);
                    Log.i("Item Clicked", "Position: " + position + " Listitem: " + x);
                    MarkerOptions marker = map.get(x);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
        });

        buildGoogleApiClient();
        googleApiClient.connect();
        createMapView();
    }

    private void buildGoogleApiClient(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void createMapView(){
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.map)).getMap();
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this, null);
        onLocationChanged(fusedLocationProviderApi.getLastLocation(googleApiClient));
        Log.i("Location", "Connected");
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            latitude.setText(location.getLatitude() + "");
            longitude.setText(location.getLongitude() + "");
            sendToGateway(location);
            Log.i("Sending", "location");
        }else{ Log.i("Location","null"); }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Location", "Suspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { Log.i("Location", "Failed"); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_data, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void sendToGateway(Location location) {
        stringForGateway = getIntent().getStringExtra("name") + ";" + location.getLatitude() + ";" + location.getLongitude();
        Log.i("Created String", stringForGateway);
        Thread broadcast = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("Thread", "Got in new thread");
                    WebSocketFactory factory = WebSocketFactory.createWebSocketFactory();
                    WebSocket broadcastSocket = factory.createWebSocket(URI.create("ws://192.168.6.135:8000/broadcast"));
                    Log.i("Socket", "Broadcast Socket");
                    broadcastSocket.connect();
                    Log.i("Socket", "Broadcast Connected");

                    WebSocket backendSocket = factory.createWebSocket(URI.create("ws://192.168.6.135:8000/backend"));
                    Log.i("Socket", "Backend Socket");
                    backendSocket.connect();
                    Log.i("Socket", "Backend Connected");

                    backendSocket.getMessageWriter().writeText(stringForGateway);
                    Log.i("Socket", "Sent Text");

                    WebSocketMessageReader reader = broadcastSocket.getMessageReader();
                    WebSocketMessageType type = reader.next();
                    if (type == WebSocketMessageType.TEXT) {
                        setMarker((String)reader.getText());
                        Log.i("Socket", "Message: " + reader.getText());
                    }
                    else if (type == WebSocketMessageType.BINARY) {
                        setMarker(new String(reader.getBinary().array()));
                        Log.i("Socket", "Message: " + new String(reader.getBinary().array()));
                    }
                } catch (Exception e) {
                    Log.i("Error", "Error: " + e);
                }
            }
        });
        broadcast.start();
    }

    private void setMarker(String x){
        final String[] s = x.split(";");
        Log.i("ListView", "Adding most recent map to list");
        runOnUiThread(new Runnable() {
            public void run() {
                map.remove(s[0]);
                namesForListView.remove(s[0]);
                googleMap.clear();
                map.put(s[0], new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(s[1]), Double.parseDouble(s[2])))
                        .title(s[0])
                        .draggable(true));

                namesForListView.add(s[0]);
                adapter.notifyDataSetChanged();

                for (MarkerOptions x : map.values()) {
                    googleMap.addMarker(x);
                    Log.i("Location", "Added: " + x.getTitle());
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(s[1]),
                        Double.parseDouble(s[2]))));
            }
        });
    }

}