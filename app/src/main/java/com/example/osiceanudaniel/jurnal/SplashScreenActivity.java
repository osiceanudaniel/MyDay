package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String TOGGLE_PATTERN_STATE = "togglePatternButton";

    private FirebaseAuth authUser;
    private FirebaseAuth.AuthStateListener authUserListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authUser = FirebaseAuth.getInstance();

        authUserListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(authUser.getCurrentUser() == null) {
                    startActivity(new Intent(SplashScreenActivity.this,
                            LoginActivity.class));
                } else {

                    loadLockScreenSharedPref();
                }
            }
        };

    }

    private void loadLockScreenSharedPref() {
        SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                MODE_PRIVATE);
        Boolean unlock = sharedPref.getBoolean(TOGGLE_PATTERN_STATE, false);
        if (unlock == true) {
            Intent intent = new Intent(getApplicationContext(), UnlockScreenActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        authUser.addAuthStateListener(authUserListener);
    }
}
