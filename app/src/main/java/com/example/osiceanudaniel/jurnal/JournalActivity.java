package com.example.osiceanudaniel.jurnal;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

	private FirebaseAuth authUser;
	private DatabaseReference usersDatabaseReference;
	private DatabaseReference notesDatabaseReference;

	private String dateString;
	private ArrayList<String> commands;
	private final String[] predefCommands = {"command clear", "command save"};

	private TextView dateEditText;
	private ImageButton voiceImageButton;
	private EditText canvas;
	private TextView usernameText;
	private Button saveBtn;

	private SpeechRecognizer speechRecognizer;
	private Intent speechIntent;

	private String displayText;

	private MenuItem clearAllMenu;

	private AlertDialog.Builder alert;
	private DialogInterface.OnClickListener dialog;
    private ProgressDialog savingNotesProgress;

	private String currentUserID;
	private Date date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		authUser = FirebaseAuth.getInstance();
		currentUserID = authUser.getUid();
		
		// reference the username of the current user in the database
		usersDatabaseReference = FirebaseDatabase.getInstance().
				getReference("Users/"+currentUserID+"/username");

		// set the notes database reference
		notesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notes");

		displayText = "";
        savingNotesProgress = new ProgressDialog(this);
        savingNotesProgress.setMessage(getString(R.string.savingNotesProgressText));

		initializeCommandsArray();
		setDialogConfirmation();
		checkPermission();

		usernameText = (TextView) findViewById(R.id.usernameEditText);
		usersDatabaseReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				String username = dataSnapshot.getValue().toString();
				usernameText.setText(username);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});


		dateEditText = (TextView) findViewById(R.id.jurnalDateEditText);
		voiceImageButton = (ImageButton) findViewById(R.id.imageButtonRecord);
		canvas = (EditText) findViewById(R.id.journalWriteEditText);
		saveBtn = (Button) findViewById(R.id.journalPostBtn);

		// action when user taps the save button
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                saveNotes();
			}
		});

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
					boolean notCommand = true;

					// check if user said a command
					String com = result.get(0).toLowerCase();
					for(int i = 0; i < commands.size(); i++) {
						if (com.equals(commands.get(i))) {
							// mark that it is a command and don't display it
							notCommand = false;
							switch (com) {
								case "command clear":
									alert.show();
									break;
								case "command save":
									Toast.makeText(getApplicationContext(),"save",Toast.LENGTH_LONG).show();
									break;
							}
						}
					}
					// first check if user said a command
					if(notCommand) {

						displayText = canvas.getText().toString();
						displayText += " " + result.get(0);
						canvas.setText(displayText);
					}
				} else {
					displayText = canvas.getText().toString();
					canvas.setText(displayText);
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

		// format the date in a specific way
		formater = new SimpleDateFormat("MMMM, hh:mm");
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

		clearAllMenu = menu.findItem(R.id.clearTextMenu);

		clearAllMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				// show the alert box to choose yes or no
				alert.show();

				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	// check mic permission
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

	// set the behaviour of the clear text dialog box
	private void setDialogConfirmation() {
		dialog = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				// get the text from the canvas first
				displayText = canvas.getText().toString();

				// set yes or no behaviour for options
				switch (i) {
					case DialogInterface.BUTTON_POSITIVE:
						displayText = "";
						canvas.setText(displayText);
						canvas.setHint(R.string.canvasHintText);

						break;
					case DialogInterface.BUTTON_NEGATIVE:
						canvas.setText(displayText);

						break;
				}
			}
		};
		alert = new AlertDialog.Builder(this);
		alert.setMessage(R.string.alertClearTextDialogMessage).setPositiveButton(R.string.alertClearTextYesOption, dialog).
				setNegativeButton(R.string.alertClearTextNoOption,dialog);
	}

	// initialize the array list of vocal commands
	private void initializeCommandsArray() {
		commands = new ArrayList<>();
		Collections.addAll(commands, predefCommands);
	}

	// save the notes to firebase database
	private void saveNotes() {
        savingNotesProgress.show();
        Log.d("Tag", "SAVING");

        // create user ID level
        DatabaseReference userIdReference = notesDatabaseReference.child(currentUserID);

        // date
        SimpleDateFormat formater;

        // format the date in a specific way
        formater = new SimpleDateFormat("MMMM, hh:mm:ss");

        // transform the date into string
        dateString = formater.format(date);

        // set child reference to date
        DatabaseReference dateDatabaseReference = userIdReference.child(dateString);

        // get the text and image form the activity
        String noteText = canvas.getText().toString();
        if (!TextUtils.isEmpty(noteText)) {
            dateDatabaseReference.child("text").setValue(noteText);
            dateDatabaseReference.child("picture").setValue("picture");

            // confirm the note has been saved
            Toast.makeText(this, this.getString(R.string.savingNotesConfirmedText),
                    Toast.LENGTH_SHORT).show();

            // redirect the user
            Intent i = new Intent(JournalActivity.this, MainPageActivity.class);
            startActivity(i);

            savingNotesProgress.dismiss();

        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.toastSave),
                    Toast.LENGTH_SHORT).show();
            Log.d("TAG2", "Dismiss");
            savingNotesProgress.dismiss();
        }
	}
}
