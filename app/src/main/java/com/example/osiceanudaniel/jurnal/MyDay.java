package com.example.osiceanudaniel.jurnal;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Osi on 4/2/2018.
 */

public class MyDay extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
