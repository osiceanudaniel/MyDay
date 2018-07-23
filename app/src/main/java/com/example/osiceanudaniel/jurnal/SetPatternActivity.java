package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class SetPatternActivity extends AppCompatActivity {

    public static final String PASSWORD_REFERENCE = "passRef";
    public static final String TOGGLE_PATTERN_STATE = "togglePatternButton";

    private PatternLockView patternLockView;
    private ToggleButton togglePattButton;
    private boolean togglePattState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pattern);

        patternLockView = (PatternLockView) findViewById(R.id.pattern_lock_view);
        togglePattButton = (ToggleButton) findViewById(R.id.toggleButtonPatternID);

//        getButtonState();
        getSharedPreferences();

        togglePattButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    patternLockView.setInputEnabled(true);
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.patternSet),
                            Toast.LENGTH_SHORT).show();
                } else {
                    patternLockView.setInputEnabled(false);
                    patternLockView.clearPattern();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.patternNotSet),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                MODE_PRIVATE);

        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                        MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(PASSWORD_REFERENCE,
                        PatternLockUtils.patternToString(patternLockView, pattern));
                editor.putBoolean(TOGGLE_PATTERN_STATE, togglePattButton.isChecked());
                editor.apply();

                Intent intent = new Intent(SetPatternActivity.this,
                        MainPageActivity.class);
                Toast.makeText(SetPatternActivity.this,
                        getString(R.string.patternCreated), Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }

            @Override
            public void onCleared() {

            }
        });
    }

    private void getSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                MODE_PRIVATE);
        Boolean buttonState = sharedPref.getBoolean(TOGGLE_PATTERN_STATE, false);
        togglePattButton.setChecked(buttonState);
        patternLockView.setInputEnabled(buttonState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences sharedPref = getSharedPreferences(SetNotificationTimeActivity.SHARED_PREFERENCES,
                MODE_PRIVATE);
        String pass = sharedPref.getString(PASSWORD_REFERENCE, "15");

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(TOGGLE_PATTERN_STATE, togglePattButton.isChecked());
        editor.apply();

        if (pass.equals("15")) {
            Toast.makeText(this, "Please", Toast.LENGTH_SHORT).show();
        }
    }

    public void getButtonState() {
        if (togglePattButton.isChecked()) {
            patternLockView.setInputEnabled(true);
        } else {
            patternLockView.setInputEnabled(false);
        }
    }
}
