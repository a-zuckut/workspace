package com.example.codewars.app;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class ShowData extends ActionBarActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    TextView longitude;
    TextView latitude;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    FusedLocationProviderApi fusedLocationProviderApi;
    String NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);
        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        Log.i("Location","3");

        buildGoogleApiClient();
        googleApiClient.connect();
        Log.i("Location", "2");
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
            Log.i("Location","1");
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
        latitude.setText(location.getLatitude() + "");
        longitude.setText(location.getLongitude() + "");
        sendToGateway(location);
        Log.i("Location", "Location Change");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Location", "Suspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Location", "Failed");
    }

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

    public void sendToGateway(Location location){
        String stringForGateway = NAME + ";" + location.getLatitude() + ";" + location.getLongitude();

    }

}