package com.example.newtrackingappjava;

import androidx.fragment.app.FragmentActivity;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.newtrackingappjava.databinding.ActivityTrackingRangeBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;

public class TrackingRangeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityTrackingRangeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrackingRangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ArrayList<Double> latitudeList = (ArrayList<Double>) getIntent().getSerializableExtra("LATITUDE_LIST");
        ArrayList<Double> longitudeList = (ArrayList<Double>) getIntent().getSerializableExtra("LONGITUDE_LIST");
        Double fuelCost = (Double) getIntent().getSerializableExtra("FUEL_COST");

        if(latitudeList != null && longitudeList != null && !latitudeList.isEmpty() && !longitudeList.isEmpty()){
            ArrayList<LatLng> points = new ArrayList<>();
            for(int i = 0; i < latitudeList.size(); i++){
                LatLng location = new LatLng(latitudeList.get(i), longitudeList.get(i));
                points.add(location);
            }

            Drawable vectorDrawableA = VectorDrawableCompat.create(getResources(), R.drawable.a, null);
            Bitmap bitmapA = Bitmap.createBitmap(vectorDrawableA.getIntrinsicWidth(),
                    vectorDrawableA.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvasA = new Canvas(bitmapA);
            vectorDrawableA.setBounds(0, 0, canvasA.getWidth(), canvasA.getHeight());
            vectorDrawableA.draw(canvasA);

            Drawable vectorDrawableB = VectorDrawableCompat.create(getResources(), R.drawable.b, null);
            Bitmap bitmapB = Bitmap.createBitmap(vectorDrawableB.getIntrinsicWidth(),
                    vectorDrawableB.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvasB = new Canvas(bitmapB);
            vectorDrawableB.setBounds(0, 0, canvasB.getWidth(), canvasB.getHeight());
            vectorDrawableB.draw(canvasB);

            int width = getResources().getDimensionPixelSize(R.dimen.marker_icon_size_width);
            int height = getResources().getDimensionPixelSize(R.dimen.marker_icon_size_height);

            Bitmap smallMarkerA = Bitmap.createScaledBitmap(bitmapA, width, height, false);
            Bitmap smallMarkerB = Bitmap.createScaledBitmap(bitmapB, width, height, false);

            LatLng firstLocation = points.get(0);

            double distance = 0;
            for (int i = 0; i < points.size() - 1; i++){
                distance += distanceHaversine(points.get(i), points.get(i + 1));
            }
            double totalFuelConsumption = (distance / 100) * fuelCost;
            String lastMarkerTitle = String.format("Пройденный путь: %.2f km, расход топлива: %.2f литров", distance, totalFuelConsumption);


            mMap.addMarker(new MarkerOptions()
                    .position(firstLocation).title(lastMarkerTitle))
                    .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarkerA));

            LatLng lastLocation = points.get(points.size() - 1);



            mMap.addMarker(new MarkerOptions()
                    .position(lastLocation).title(lastMarkerTitle))
                    .setIcon(BitmapDescriptorFactory.fromBitmap(smallMarkerB));



            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(points)
                    .color(0xFF0000FF)
                    .width(20));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 16f));
        }
    }

    public double distanceHaversine(LatLng start, LatLng end){
        final int R = 6371;

        double latDistance = Math.toRadians(end.latitude - start.latitude);
        double lonDistance = Math.toRadians(end.longitude - start.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }
}