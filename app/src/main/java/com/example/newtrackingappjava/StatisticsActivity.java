package com.example.newtrackingappjava;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrackingappjava.Interface.IFirebaseLoadDone;
import com.example.newtrackingappjava.Interface.IRecyclerItemClickListener;
import com.example.newtrackingappjava.Interface.IRecyclerItemLongClickListener;
import com.example.newtrackingappjava.Model.User;
import com.example.newtrackingappjava.Utils.Common;
import com.example.newtrackingappjava.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity implements  IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_friend_list;
    IFirebaseLoadDone firebaseLoadDone;
    MaterialSearchBar searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);


        recycler_friend_list = (RecyclerView) findViewById(R.id.recycler_friend_list);
        recycler_friend_list.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_friend_list.setLayoutManager(layoutManager);
        recycler_friend_list.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        firebaseLoadDone = this;

        loadFriendList();

    }


    private void loadFriendList() {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggerUser.getUid())
                .child(Common.ACCEPT_LIST);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                holder.txt_user_transport.setText(new StringBuilder(model.getTransportName()));



                DatabaseReference onlineStatusRef = FirebaseDatabase.getInstance()
                        .getReference(Common.USER_INFORMATION)
                        .child(model.getUid())
                        .child("online");

                onlineStatusRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.getValue(Boolean.class) != null){
                            boolean online = snapshot.getValue(Boolean.class);
                            if(online){
                                holder.img_online_status.setImageResource(R.drawable.online_status);
                            }else{
                                holder.img_online_status.setImageResource(R.drawable.offline_status);
                            }
                        }else {
                            holder.img_online_status.setImageResource(R.drawable.offline_status);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                holder.setiRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        Common.trackingUser = model;
                        startActivity(new Intent(StatisticsActivity.this, StatisticsListActivity.class));
                    }
                });

                holder.setiRecyclerItemLongClickListener(new IRecyclerItemLongClickListener() {
                    @Override
                    public void onItemLongClickListener(View view, int position) {
                        showDialogDateRange(model);
                    }
                });

            }
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };

        adapter.startListening();
        recycler_friend_list.setAdapter(adapter);
    }



    @Override
    protected void onStart() {
        super.onStart();
        updateOnlineStatus(true);
        if (adapter != null) {
            adapter.startListening();
        }
        if (searchAdapter != null) {
            searchAdapter.startListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
        if (searchAdapter != null) {
            searchAdapter.stopListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            updateOnlineStatus(false);
        }
        if(adapter != null){
            adapter.stopListening();
        }
        if(searchAdapter != null)
            searchAdapter.stopListening();
        recycler_friend_list.setItemAnimator(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            updateOnlineStatus(false);
        }
    }

    private void updateOnlineStatus(boolean isOnline) {
        DatabaseReference onlineRef = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggerUser.getUid())
                .child("online");
        onlineRef.setValue(isOnline);
    }


    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail, List<String> lstTransport) {
        searchBar.setLastSuggestions(lstEmail);

    }

    @Override
    public void onFirebaseLoadFailed(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    private void showDialogDateRange(User model){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyRequestDialog);
        alertDialogBuilder.setTitle("Выберите диапазон поездок");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_range, null);
        alertDialogBuilder.setView(dialogView);

        TextView txtStartDate = dialogView.findViewById(R.id.txtStartDate);
        TextView txtStartTime = dialogView.findViewById(R.id.txtStartTime);
        TextView txtEndDate = dialogView.findViewById(R.id.txtEndDate);
        TextView txtEndTime = dialogView.findViewById(R.id.txtEndTime);
        Button btnOk = dialogView.findViewById(R.id.btnOk);

        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();

        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StatisticsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                startCalendar.set(Calendar.YEAR, year);
                                startCalendar.set(Calendar.MONTH, month);
                                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                txtStartDate.setText(sdf.format(startCalendar.getTime()));
                            }
                        },
                        startCalendar.get(Calendar.YEAR),
                        startCalendar.get(Calendar.MONTH),
                        startCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        txtStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        StatisticsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                startCalendar.set(Calendar.MINUTE, minute);
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                txtStartTime.setText(sdf.format(startCalendar.getTime()));
                            }
                        },
                        startCalendar.get(Calendar.HOUR_OF_DAY),
                        startCalendar.get(Calendar.MINUTE),
                        true);
                timePickerDialog.show();
            }
        });

        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        StatisticsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                endCalendar.set(Calendar.YEAR, year);
                                endCalendar.set(Calendar.MONTH, month);
                                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                txtEndDate.setText(sdf.format(endCalendar.getTime()));
                            }
                        },
                        endCalendar.get(Calendar.YEAR),
                        endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        txtEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        StatisticsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                endCalendar.set(Calendar.MINUTE, minute);
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                txtEndTime.setText(sdf.format(endCalendar.getTime()));
                            }
                        },
                        endCalendar.get(Calendar.HOUR_OF_DAY),
                        endCalendar.get(Calendar.MINUTE),
                        true);
                timePickerDialog.show();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startDateInMillis = startCalendar.getTimeInMillis();
                long endDateInMillis = endCalendar.getTimeInMillis();

                if(startDateInMillis < endDateInMillis){
                    statisticsPoints(model, startDateInMillis, endDateInMillis);
                } else{
                    Toast.makeText(StatisticsActivity.this, "Пожалуйста, выберите корректный диапазон времени", Toast.LENGTH_SHORT).show();
                }

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void statisticsPoints(User user, long startDateInMillis, long endDateInMillis) {
            String userId = user.getUid();
            DatabaseReference userStatisticsLocation = FirebaseDatabase.getInstance().getReference(Common.STATISTICS_LOCATION).child(userId);
            DatabaseReference userInformationRef = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION).child(userId);

            userInformationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userInfoSnapshot) {
                    if (userInfoSnapshot.exists()) {
                        Double fuelCost = null;
                        if (userInfoSnapshot.child("acceptList").exists()) {
                            for (DataSnapshot acceptListSnapshot : userInfoSnapshot.child("acceptList").getChildren()) {
                                Double currentFuelCost = acceptListSnapshot.child("fuelCost").getValue(Double.class);
                                if (currentFuelCost != null) {
                                    fuelCost = currentFuelCost;
                                    break;
                                }
                            }
                        }
                        final Double finalFuelCost = fuelCost;

                        userStatisticsLocation.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Long statisticsCount = snapshot.child("statisticsCount").getValue(Long.class);

                                    if (statisticsCount != null) {
                                        int maxRecords = 0;

                                        while (snapshot.child("latitude" + maxRecords).exists() &&
                                                snapshot.child("longitude" + maxRecords).exists() &&
                                                snapshot.child("time" + maxRecords).exists()) {
                                            maxRecords++;
                                        }

                                        List<Double> latitudeList = new ArrayList<>();
                                        List<Double> longitudeList = new ArrayList<>();
                                        List<Double> timeList = new ArrayList<>();


                                        for (int i = 0; i < maxRecords; i++) {
                                            Long time = snapshot.child("time" + i).getValue(Long.class);
                                            if (time != null && time >= startDateInMillis && time <= endDateInMillis) {
                                                Double latitude = snapshot.child("latitude" + i).getValue(Double.class);
                                                Double longitude = snapshot.child("longitude" + i).getValue(Double.class);
                                                if (latitude != null && longitude != null) {
                                                    latitudeList.add(latitude);
                                                    longitudeList.add(longitude);
                                                    timeList.add(Double.valueOf(time));
                                                }
                                            }
                                        }
                                        Intent intent = new Intent(StatisticsActivity.this, TrackingRangeActivity.class);
                                        intent.putExtra("LATITUDE_LIST", (Serializable) latitudeList);
                                        intent.putExtra("LONGITUDE_LIST", (Serializable) longitudeList);
                                        intent.putExtra("TIME_LIST", (Serializable) timeList);
                                        intent.putExtra("FUEL_COST", finalFuelCost);

                                        startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(StatisticsActivity.this, "Данные статистики местоположения отсутствуют", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(StatisticsActivity.this, "Не удалось загрузить данные статистики местоположения: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(StatisticsActivity.this, "Данные пользователя отсутствуют", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StatisticsActivity.this, "Не удалось загрузить данные пользователя: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}