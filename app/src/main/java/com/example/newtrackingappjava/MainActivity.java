package com.example.newtrackingappjava;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrackingappjava.Utils.Common;
import com.example.newtrackingappjava.Model.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    DatabaseReference userInformation;
    FirebaseAuth fAuth;
    EditText mTransportName, mEmail, mPassword, mFuelCost;
    Button mRegisterBtn;
    TextView mLoginBtn;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Paper.init(this);
        fAuth = FirebaseAuth.getInstance();
        userInformation = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        mTransportName = findViewById(R.id.transportName_input);
        mFuelCost = findViewById(R.id.fuelCost_input);
        mEmail = findViewById(R.id.email_input);
        mPassword = findViewById(R.id.password_input);
        mRegisterBtn = findViewById(R.id.registerButton);
        mLoginBtn = findViewById(R.id.loginText);
        String savedUid = Paper.book().read(Common.USER_UID_SAVE_KEY);
        if (!TextUtils.isEmpty(savedUid)) {
            // Проверить наличие пользователя в Firebase
            userInformation.child(savedUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Common.loggerUser = dataSnapshot.getValue(User.class);
                        // Переход на главный экран приложения
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // UID не найден в базе данных, выполнить обычный запуск
                        initUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Ошибка чтения данных из базы данных: " + databaseError.getMessage());
                    Toast.makeText(MainActivity.this, "Ошибка! " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    // В случае ошибки выполнить обычный запуск
                    initUI();
                }
            });
        } else {
            // Если UID нет, выполнить обычный запуск
            initUI();
        }
    }
    private void initUI() {
        // Запрос разрешения на геолокацию
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                registerUser();
                            }
                        });
                        mLoginBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            }
                        });
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Для работы приложения необходимо разрешение на геолокацию", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }
    private void registerUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String transportName = mTransportName.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Поле Email обязательно для ввода!");
            return;
        }
        if (password.length() < 6) {
            mPassword.setError("Пароль должен содержать 6 и более символов!");
            return;
        }
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Пользователь создан!", Toast.LENGTH_SHORT).show();
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                userInformation.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Common.loggerUser = dataSnapshot.getValue(User.class);
                                        } else {
                                            String email = firebaseUser.getEmail();
                                            String transportName = mTransportName.getText().toString().trim();
                                            int fuelCost = Integer.parseInt(mFuelCost.getText().toString().trim());
                                            Common.loggerUser = new User(uid, email, transportName, fuelCost);
                                            userInformation.child(uid).setValue(Common.loggerUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Данные пользователя успешно сохранены.");
                                                    } else {
                                                        Log.e(TAG, "Ошибка сохранения данных пользователя: ", task.getException());
                                                        Toast.makeText(MainActivity.this, "Ошибка!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        Paper.book().write(Common.USER_UID_SAVE_KEY, Common.loggerUser.getUid());
                                        updateToken(firebaseUser);
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "Ошибка чтения данных из базы данных: " + databaseError.getMessage());
                                        Toast.makeText(MainActivity.this, "Ошибка! " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Log.e(TAG, "Текущий пользователь равен null");
                                Toast.makeText(MainActivity.this, "Ошибка! Текущий пользователь равен null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Ошибка регистрации пользователя: ", task.getException());
                            Toast.makeText(MainActivity.this, "Ошибка! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateToken(FirebaseUser firebaseUser) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        tokens.child(firebaseUser.getUid()).setValue(instanceIdResult.getToken());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Ошибка обновления токена пользователя: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Ошибка! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
