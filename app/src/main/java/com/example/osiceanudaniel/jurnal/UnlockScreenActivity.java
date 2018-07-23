package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class UnlockScreenActivity extends AppCompatActivity {

    public static final String PASSWORD_REFERENCE = "passRef";

    private PatternLockView patternLockView;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_screen);

        SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                MODE_PRIVATE);
        pass = sharedPref.getString(PASSWORD_REFERENCE, "15");

        patternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (pass.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {

                    Intent intent = new Intent(UnlockScreenActivity.this,
                            MainPageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    patternLockView.clearPattern();
                    Toast.makeText(UnlockScreenActivity.this,
                            getString(R.string.wrongPattern), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCleared() {

            }
        });
    }
}
