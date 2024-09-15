package com.example.newtrackingappjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.example.newtrackingappjava.Utils.Common;
import com.example.newtrackingappjava.databinding.ActivityStatisticsTrackingBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class TrackingStatisticsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private ActivityStatisticsTrackingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatisticsTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            selectedLatitude = intent.getDoubleExtra("latitude", 0.0);
            selectedLongitude = intent.getDoubleExtra("longitude", 0.0);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Enable zoom UI
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (selectedLatitude != 0.0 && selectedLongitude != 0.0) {
            LatLng selectedLocation = new LatLng(selectedLatitude, selectedLongitude);
            mMap.addMarker(new MarkerOptions().position(selectedLocation).title(Common.trackingUser.getTransportName()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 16f));
        }

    }
}