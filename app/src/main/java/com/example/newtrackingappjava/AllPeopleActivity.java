package com.example.newtrackingappjava;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrackingappjava.Interface.IFirebaseLoadDone;
import com.example.newtrackingappjava.Interface.IRecyclerItemClickListener;
import com.example.newtrackingappjava.Model.MyResponse;
import com.example.newtrackingappjava.Model.Request;
import com.example.newtrackingappjava.Model.User;
import com.example.newtrackingappjava.Remote.IFCMService;
import com.example.newtrackingappjava.Utils.Common;
import com.example.newtrackingappjava.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import io.reactivex.Flowable;
//import rx.schedulers.Schedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
//import rx.Observable;
import io.reactivex.Observable;
//import rx.android.schedulers.AndroidSchedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.search.SearchView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.rxjava3.core.Scheduler;


import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;


import org.checkerframework.checker.units.qual.A;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

//import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_all_user;
    IFirebaseLoadDone firebaseLoadDone;
    MaterialSearchBar searchBar;

    List<String> suggestList = new ArrayList<>();

    IFCMService ifcmService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);

        // InitApi
        ifcmService = Common.getFCMService();

        // Init view user
        searchBar = findViewById(R.id.material_search_bar);
        searchBar.setCardViewElevation(10);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text_search = charSequence.toString().toLowerCase();
                List<String> suggest = new ArrayList<>();
                for (String search : suggestList) {
                    if (search.toLowerCase().contains(text_search)) {
                        suggest.add(search);
                    }
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    if (adapter != null) {
                        recycler_all_user.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {}
        });

        recycler_all_user = findViewById(R.id.recycler_all_people);
        recycler_all_user.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        firebaseLoadDone = this;

        loadUserList();
        loadSearchData();
    }

    private void loadSearchData() {
        final List<String> lstEmailName = new ArrayList<>();
        final List<String> lstTransportName = new ArrayList<>();
        DatabaseReference userList = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION)
                .child(Common.loggerUser.getUid())
                .child(Common.FRIEND_REQUEST);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                    User user = userSnapShot.getValue(User.class);
                    lstEmailName.add(user.getEmail());
                    lstTransportName.add(user.getTransportName());
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstEmailName, lstTransportName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull User model) {
                if (model != null && model.getEmail() != null) {
                    if (model.getEmail().equals(Common.loggerUser.getEmail())) {
                        holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append(" (me) "));
                        holder.txt_user_transport.setText(new StringBuilder(model.getTransportName()));
                        holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                        holder.txt_user_transport.setTypeface(holder.txt_user_transport.getTypeface(), Typeface.ITALIC);
                    } else {
                        holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                        holder.txt_user_transport.setText(new StringBuilder(model.getTransportName()));
                    }
                    holder.setiRecyclerItemClickListener((view, position) -> {
                        showDialogRequest(model);
                    });
                } else {
                    Log.e("AllPeopleActivity", "model or model.getTransport() is null!");
                }
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
        recycler_all_user.setAdapter(adapter);
    }

    private void showDialogRequest(User model) {
        if (model.getUid().equals(Common.loggerUser.getUid())) {
            Toast.makeText(this, "Вы не можете добавить себя в друзья", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyRequestDialog);
        alertDialogBuilder.setTitle("Запросить друга");
        alertDialogBuilder.setMessage("Вы хотите отправить запрос другу по адресу " + model.getEmail());
        alertDialogBuilder.setIcon(R.drawable.baseline_account_circle_24);

        alertDialogBuilder.setNegativeButton("Отмена", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialogBuilder.setPositiveButton("Отправить", (dialogInterface, i) -> {
            DatabaseReference acceptList = FirebaseDatabase.getInstance()
                    .getReference(Common.USER_INFORMATION)
                    .child(Common.loggerUser.getUid())
                    .child(Common.ACCEPT_LIST);
            acceptList.orderByKey().equalTo(model.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null) {
                                sendFriendRequest(model);
                            } else {
                                Toast.makeText(AllPeopleActivity.this, "Вы и " + model.getEmail() + " теперь друзья", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Обработка ошибок, если необходимо
                        }
                    });
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            // Установка цвета текста для кнопок
            positiveButton.setTextColor(getResources().getColor(R.color.purple));
            negativeButton.setTextColor(getResources().getColor(R.color.purple));
        });

        alertDialog.show();
    }

    private void sendFriendRequest(final User model) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS);
        tokens.orderByKey().equalTo(model.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            Toast.makeText(AllPeopleActivity.this, "Tokens error", Toast.LENGTH_SHORT).show();
                        } else {
                            Request request = new Request();
                            // Создание данных
                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put(Common.FROM_UID, Common.loggerUser.getUid());
                            dataSend.put(Common.FROM_NAME, Common.loggerUser.getEmail());
                            dataSend.put(Common.FROM_TRANSPORT, Common.loggerUser.getTransportName());
                            dataSend.put(Common.TO_UID, model.getUid());
                            dataSend.put(Common.TO_NAME, model.getEmail());
                            dataSend.put(Common.TO_TRANSPORT, model.getTransportName());

                            request.setTo(dataSnapshot.child(model.getUid()).getValue(String.class));
                            request.setData(dataSend);

                            compositeDisposable.add(ifcmService.sendFriendRequestToUser(request)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(myResponse -> {
                                        if (myResponse.success == 1) {
                                            Toast.makeText(AllPeopleActivity.this, "Запрос отправлен!", Toast.LENGTH_SHORT).show();
                                        }
                                    }, throwable -> {
                                        Toast.makeText(AllPeopleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Обработка отмены запроса (необязательно)
                    }
                });
    }

    @Override
    protected void onStop() {
        if (adapter != null) adapter.stopListening();
        if (searchAdapter != null) searchAdapter.stopListening();
        recycler_all_user.setItemAnimator(null);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.startListening();
        if (searchAdapter != null) searchAdapter.startListening();
    }

    private void startSearch(String text_search) {
        Query query = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .orderByChild("transportName")
                .startAt(text_search)
                .endAt(text_search + "\uf8ff");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int i, @NonNull final User model) {
                if (model != null && model.getEmail() != null) {
                    if (model.getEmail().equals(Common.loggerUser.getEmail())) {
                        holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append(" (me) "));
                        holder.txt_user_transport.setText(new StringBuilder(model.getTransportName()));
                        holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                        holder.txt_user_transport.setTypeface(holder.txt_user_transport.getTypeface(), Typeface.ITALIC);
                    } else {
                        holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                        holder.txt_user_transport.setText(new StringBuilder(model.getTransportName()));
                    }
                    holder.setiRecyclerItemClickListener((view, position) -> {
                        showDialogRequest(model);
                    });
                } else {
                    Log.e("AllPeopleActivity", "model or model.getTransport() is null!");
                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);
    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail, List<String> lstTransport) {
        searchBar.setLastSuggestions(lstEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
