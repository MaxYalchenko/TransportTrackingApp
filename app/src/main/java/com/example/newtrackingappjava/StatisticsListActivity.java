package com.example.newtrackingappjava;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrackingappjava.Model.MyLocation;
import com.example.newtrackingappjava.Model.MyStatLocation;
import com.example.newtrackingappjava.Utils.Common;
import com.example.newtrackingappjava.Utils.LocationAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

public class StatisticsListActivity extends AppCompatActivity implements LocationAdapter.OnItemClickListener {

    private RecyclerView locationList;
    private LocationAdapter adapter;
    private List<MyStatLocation> locationDataList;
    private DatabaseReference statisticsLocation;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_list);

        locationList = findViewById(R.id.location_list);
        locationList.setLayoutManager(new LinearLayoutManager(this));
        locationDataList = new ArrayList<>();
        adapter = new LocationAdapter(locationDataList);
        locationList.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        if (Common.trackingUser != null) {
            uid = Common.trackingUser.getUid();
        } else {
            uid = Paper.book().read(Common.USER_UID_SAVE_KEY);
        }

        statisticsLocation = FirebaseDatabase.getInstance().getReference(Common.STATISTICS_LOCATION).child(uid);

        loadStatisticsData();
    }

    private void loadStatisticsData() {

        long currentTime = System.currentTimeMillis();
        long oneDayBefore = currentTime - (24 * 60 * 60 * 1000);
        long fiveMinutesInMilliseconds = 5 * 60 * 1000;

        Map<Long, MyStatLocation> filteredData = new HashMap<>();

        statisticsLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationDataList.clear();
                filteredData.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    if (postSnapshot.getKey().startsWith("time")) {
                        long time = postSnapshot.getValue(Long.class);
                        if (time >= oneDayBefore && time <= currentTime) {
                            long roundedTime = time - (time % fiveMinutesInMilliseconds);

                            if (!filteredData.containsKey(roundedTime)) {
                                String index = postSnapshot.getKey().substring(4);
                                Double latitude = snapshot.child("latitude" + index).getValue(Double.class);
                                Double longitude = snapshot.child("longitude" + index).getValue(Double.class);
                                if (latitude != null && longitude != null) {
                                    MyStatLocation locationData = new MyStatLocation(latitude, longitude, time);
                                    filteredData.put(roundedTime, locationData);
                                }
                            }
                        }
                    }
                }

                for (MyStatLocation locationData : filteredData.values()) {
                    locationDataList.add(locationData);
                }

                Collections.sort(locationDataList, new Comparator<MyStatLocation>() {
                    @Override
                    public int compare(MyStatLocation o1, MyStatLocation o2) {
                        return Long.compare(o1.getTime(), o2.getTime());
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        MyStatLocation selectedLocation = locationDataList.get(position);
        Intent intent = new Intent(StatisticsListActivity.this, TrackingStatisticsActivity.class);
        intent.putExtra("latitude", selectedLocation.getLatitude());
        intent.putExtra("longitude", selectedLocation.getLongitude());
        startActivity(intent);
    }
}
