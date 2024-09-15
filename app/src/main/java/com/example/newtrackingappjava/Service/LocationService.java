package com.example.newtrackingappjava.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.newtrackingappjava.Model.User;
import com.example.newtrackingappjava.R;
import com.example.newtrackingappjava.Utils.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import io.paperdb.Paper;

public class LocationService extends Service {

    private static final int NOTIFICATION_ID = 1;
    String uid;
    private static final String CHANNEL_ID = "location_channel";
    DatabaseReference publicLocation;
    DatabaseReference statisticsLocation;
    DatabaseReference statisticsCountRef;
    private static final int MAX_RECORDS = 536000;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private int statisticsCount = 0;
    private boolean isLocationCallbackSet = false;


    @Override
    public void onCreate() {
        super.onCreate();
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        statisticsLocation = FirebaseDatabase.getInstance().getReference(Common.STATISTICS_LOCATION);
        Paper.init(this);
        uid = Paper.book().read(Common.USER_UID_SAVE_KEY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking location")
                .setContentText("Location service is running")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .build();

        // Start foreground service
        startForeground(NOTIFICATION_ID, notification);

        buildLocationRequest();
        if (!isLocationCallbackSet) {
            buildLocationRequest();
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    Location location = locationResult.getLastLocation();
                    HashMap<String, Object> locationData = new HashMap<>();
                    locationData.put("latitude", location.getLatitude());
                    locationData.put("longitude", location.getLongitude());
                    locationData.put("time", location.getTime());
                    // Отправка location в Firebase
                    if (Common.loggerUser != null) {
                        publicLocation.child(Common.loggerUser.getUid()).setValue(locationData);
                    } else {
                        // Использование uid, полученный из Paper
                        publicLocation.child(uid).setValue(locationData);
                    }

                    writeStatisticsLocation(location);
                }
            };
            updateLocation();
            isLocationCallbackSet = true;
        }
        getStatisticsCountAndUpdateLocation();
    }



    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void updateLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uid = intent.getStringExtra("uid");
        Paper.init(this);
        publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        statisticsLocation = FirebaseDatabase.getInstance().getReference(Common.STATISTICS_LOCATION);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getStatisticsCountAndUpdateLocation(){
        DatabaseReference userStatisticsLocation = statisticsLocation.child(uid != null ? uid : Common.loggerUser.getUid());
        statisticsCountRef = userStatisticsLocation.child("statisticsCount");

        statisticsCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    statisticsCount = dataSnapshot.getValue(Integer.class);
                } else {
                    statisticsCount = 0;
                }
                updateLocation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateLocation();
            }
        });
    }
    private void writeStatisticsLocation(Location location) {
        int index = statisticsCount % MAX_RECORDS;
        HashMap<String, Object> locationData = new HashMap<>();
        locationData.put("latitude" + index, location.getLatitude());
        locationData.put("longitude" + index, location.getLongitude());
        locationData.put("time" + index, location.getTime());

        if (Common.loggerUser != null) {
            statisticsLocation.child(Common.loggerUser.getUid()).updateChildren(locationData);
        } else {
            // Использование uid, полученный из Paper
            statisticsLocation.child(uid).updateChildren(locationData);
        }
        statisticsCount++;
        statisticsCountRef.setValue(statisticsCount);
    }

}