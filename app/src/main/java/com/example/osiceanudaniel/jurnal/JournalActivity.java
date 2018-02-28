package com.example.osiceanudaniel.jurnal;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

	private String dateString;

	private TextView dateEditText;
	private ImageButton voiceImageButton;
	private EditText canvas;

	private SpeechRecognizer speechRecognizer;
	private Intent speechIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		checkPermission();

		dateEditText = (TextView) findViewById(R.id.jurnalDateEditText);
		voiceImageButton = (ImageButton) findViewById(R.id.imageButtonRecord);
		canvas = (EditText) findViewById(R.id.journalWriteEditText);

		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

		speechRecognizer.setRecognitionListener(new RecognitionListener() {
			@Override
			public void onReadyForSpeech(Bundle params) {

			}

			@Override
			public void onBeginningOfSpeech() {

			}

			@Override
			public void onRmsChanged(float rmsdB) {

			}

			@Override
			public void onBufferReceived(byte[] buffer) {

			}

			@Override
			public void onEndOfSpeech() {

			}

			@Override
			public void onError(int error) {

			}

			@Override
			public void onResults(Bundle results) {
				ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

				// if it recognized something, get the first result that is the best match
				// and display it
				if(!result.isEmpty()) {
					canvas.setText(result.get(0));
				}
			}

			@Override
			public void onPartialResults(Bundle partialResults) {

			}

			@Override
			public void onEvent(int eventType, Bundle params) {

			}
		});

		voiceImageButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN :
						canvas.setText("");
						canvas.setHint(R.string.listeningImageButtonTextDown);
						speechRecognizer.startListening(speechIntent);
						break;
					case MotionEvent.ACTION_UP :
						speechRecognizer.stopListening();
						break;
				}
				return false;
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		// disable app name on Action bar
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		SimpleDateFormat formater;
		Date date;

		// format the date in a specific way
		formater = new SimpleDateFormat("dd/MM/yyyy, hh:mm:ss aaa");
		// get the current date
		date = new Date();

		// transform the date into string
		dateString = formater.format(date);

		// show the date
		dateEditText.setText(dateString);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// show the menu items
		getMenuInflater().inflate(R.menu.journal_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	private void checkPermission() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
					== PackageManager.PERMISSION_GRANTED)) {
				Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
						Uri.parse("package:" + getPackageName()));
				startActivity(i);
				finish();
			}
		}
	}
}
