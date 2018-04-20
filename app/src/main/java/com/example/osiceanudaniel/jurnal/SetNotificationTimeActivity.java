package com.example.osiceanudaniel.jurnal;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

public class SetNotificationTimeActivity extends AppCompatActivity{

    public static final String SHARED_PREFERENCES = "sharedPreferences";
    public static final String HOUR_PREFERENCE = "hourPref";
    public static final String MINUTE_PREFERENCE = "minutePref";
    public static final String TOGGLE_STATE = "toggleButton";

    private TextView timeTextView;
    private ToggleButton toggleButton;
    private Button saveButton;

    private int hour = 18;
    private int min = 00;

    private boolean toggleState;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_notification_time);

        timeTextView = (TextView) findViewById(R.id.timeTextView);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButtonID);
        saveButton = (Button) findViewById(R.id.saveTimeBtnID);

        loadData();
        updateInterface();

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(SetNotificationTimeActivity.this,
                        listener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true).show();
            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.notificationSet),
                            Toast.LENGTH_SHORT).show();
                    timeTextView.setEnabled(true);
                    timeTextView.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    timeTextView.setEnabled(false);
                    timeTextView.setTextColor(Color.parseColor("#A0A0A0"));
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.notificationNotSet),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotification();
            }
        });
    }

    private void saveNotification() {
        // no other program can change our preference
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(HOUR_PREFERENCE, hour);
        editor.putInt(MINUTE_PREFERENCE, min);
        editor.putBoolean(TOGGLE_STATE, toggleButton.isChecked());

        editor.apply();

        if (toggleButton.isChecked()) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.notificationEnabled),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.notificationDisabled),
                    Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(SetNotificationTimeActivity.this,
                MainPageActivity.class));

    }

    private void loadData() {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        hour = sharedPref.getInt(HOUR_PREFERENCE, 18);
        min = sharedPref.getInt(MINUTE_PREFERENCE, 0);
        toggleState = sharedPref.getBoolean(TOGGLE_STATE, true);
    }

    private void updateInterface() {
        if (min < 10 && (hour < 10 || hour == 0) ) {
            timeTextView.setText("0" + hour + ":0" + min);
        } else if(min < 10) {
            timeTextView.setText(hour + ":0" + min);
        } else if (hour < 10 || hour == 0) {
            timeTextView.setText("0" + hour + ":" + min);
        } else {
            timeTextView.setText(hour + ":" + min);
        }

        toggleButton.setChecked(toggleState);
    }

    TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;

            if (min < 10 && (hour < 10 || hour == 0) ) {
                timeTextView.setText("0" + hour + ":0" + min);
            } else if(min < 10) {
                timeTextView.setText(hour + ":0" + min);
            } else if (hour < 10 || hour == 0) {
                timeTextView.setText("0" + hour + ":" + min);
            } else {
                timeTextView.setText(hour + ":" + min);
            }
        }
    };

}
