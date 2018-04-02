package com.example.osiceanudaniel.jurnal;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

	private final String[] predefCommands = {"command clear", "command save"};
	private final static int IMAGE_GALLERY_REQUEST_CODE = 20;
	private final static int CAMERA_REQUEST_CODE = 30;
	private final static int RECORD_AUDIO_REQUEST_CODE = 40;

	private FirebaseAuth authUser;
	private DatabaseReference usersDatabaseReference;
	private StorageReference mainStorageReference;

	private DatabaseReference notesDatabaseReference;
	private String dateString;
	private ArrayList<String> commands;
    private Uri imageURI;

	private TextView dateEditText;
	private ImageButton voiceImageButton;
	private EditText canvas;
	private TextView usernameText;
	private ImageView displayPicture;
	private ImageButton cameraImageButton;

	private SpeechRecognizer speechRecognizer;
	private Intent speechIntent;

	private String displayText;

	private MenuItem clearAllMenu;
	private MenuItem saveMenu;

	private AlertDialog.Builder alert;
	private DialogInterface.OnClickListener dialog;
    private ProgressDialog savingNotesProgress;

	private String currentUserID;
	private Date date;

	private int permissionCamera;
	private int permissionAudio;
	private int permissionStorage;

    private String mCurrentPhotoPath;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_journal);

		// get the permissions first
        permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        permissionAudio = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        permissionStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // make app compatible with android oreo and above
        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		authUser = FirebaseAuth.getInstance();
		currentUserID = authUser.getUid();
		mainStorageReference = FirebaseStorage.getInstance().getReference();
		
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
		displayPicture = (ImageView) findViewById(R.id.PictureImageView);
		cameraImageButton = (ImageButton) findViewById(R.id.CameraImageButton);

		displayPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, IMAGE_GALLERY_REQUEST_CODE);
                } else {
                    chooseSetImage();
                }
            }
        });

		cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checkCameraPermission();
                if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
                    askForPermission(Manifest.permission.CAMERA,CAMERA_REQUEST_CODE);
                } else if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,IMAGE_GALLERY_REQUEST_CODE);
                } else {
                    capturePhoto();
                }
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
                if (permissionAudio != PackageManager.PERMISSION_GRANTED) {
                    askForPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_REQUEST_CODE);
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            canvas.setHint(R.string.listeningImageButtonTextDown);
                            speechRecognizer.startListening(speechIntent);
                            break;
                        case MotionEvent.ACTION_UP:
                            speechRecognizer.stopListening();
                            break;
                    }
                }
                return false;
            }
		});
	}


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        permissionAudio = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        permissionStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
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

        Log.e("TAHBSSA", "ON START CALLED");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the permissions again
        permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        permissionAudio = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        permissionStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Log.e("TAT", "ON RESUME CALLED");
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// show the menu items
		getMenuInflater().inflate(R.menu.journal_menu, menu);

		clearAllMenu = menu.findItem(R.id.clearTextMenu);
		saveMenu = menu.findItem(R.id.saveMenuButton);

		clearAllMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				// show the alert box to choose yes or no
				alert.show();

				return false;
			}
		});

		saveMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				// save note to firebase
                if(imageURI != null && (!TextUtils.isEmpty(canvas.getText().toString()))) {
                    saveNotes();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.writeSomethigOrPickImage),
                            Toast.LENGTH_SHORT).show();
                }

				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            imageURI = data.getData();
            Glide.with(getApplicationContext())
                    .load(imageURI)
                    .centerCrop()
                    .into(displayPicture);
            Log.e("TASSF", "IMAG GALLERY IMAGE URI: " + imageURI);
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // display the picture from the storage
            imageURI = Uri.parse(mCurrentPhotoPath);
            Glide.with(getApplicationContext())
                    .load(imageURI)
                    .centerCrop()
                    .into(displayPicture);
            Log.e("TASSF", "CAMERA IMAGE URI: " + imageURI);
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(JournalActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(JournalActivity.this, permission)) {

                // Ask the permission again if denied before
                ActivityCompat.requestPermissions(JournalActivity.this, new String[]{permission}, requestCode);

            } else {
                ActivityCompat.requestPermissions(JournalActivity.this, new String[]{permission}, requestCode);
            }
            return;
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

        // get the storage reference for photos
        StorageReference imageStorageFilePath = mainStorageReference.child("NotesPhotos")
                .child(imageURI.getLastPathSegment());
        imageStorageFilePath.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadableUri = taskSnapshot.getDownloadUrl();

                // create user ID level
                DatabaseReference userIdReference = notesDatabaseReference.child(currentUserID);

                // date
                SimpleDateFormat formater;
                SimpleDateFormat formater2;

                // format the date in a specific way
					formater = new SimpleDateFormat("EEE, d MMM yyyy hh:mm");

                // transform the date into string
                dateString = formater.format(date);

                // format date for save
				formater2 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_SSS");
				String dateStringChild = formater2.format(date);

                // set child reference to date
                DatabaseReference dateDatabaseReference = userIdReference.child(dateStringChild);

                dateDatabaseReference.child("text").setValue(canvas.getText().toString());
                dateDatabaseReference.child("picture").setValue(downloadableUri.toString());
                dateDatabaseReference.child("data").setValue(dateString);

                // confirm the note has been saved
                Toast.makeText(getApplicationContext(), getString(R.string.savingNotesConfirmedText),
                        Toast.LENGTH_SHORT).show();

                // redirect the user
                Intent i = new Intent(JournalActivity.this, MainPageActivity.class);
                startActivity(i);

                savingNotesProgress.dismiss();

//                Toast.makeText(getApplicationContext(), getString(R.string.toastSave),
//                        Toast.LENGTH_SHORT).show();
                Log.d("TAG2", "Dismiss");
//                savingNotesProgress.dismiss();
            }
        });
	}

	// open image gallery and select an image
    private void chooseSetImage() {
        Intent galleryI = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryI, IMAGE_GALLERY_REQUEST_CODE);
    }

	// create the file in Pictures folder
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",    // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    // start the camera and display the photo
    private void capturePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("TAG", "");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

}
