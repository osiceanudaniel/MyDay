package com.example.osiceanudaniel.jurnal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditNoteActivity extends AppCompatActivity {

    public static final int IMAGE_GALLERY_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 110;

    private EditText editText;
    private ImageView imageView;
    private ImageButton cameraBtn;
    private Button saveBtn;

    private FirebaseAuth auth;
    private StorageReference notesImageStorege;
    private String currentUserId;

    private String oldImageURL;
    private String oldText;

    private Uri imageURI = null;
    private String mCurrentPhotoPath;
    private String postKey;
    private String displayText;

    private ProgressDialog progressDialog;
    private MenuItem clearTextMenu;
    private AlertDialog.Builder alert;
    private DialogInterface.OnClickListener dialogClearText;

    private AlertDialog.Builder confirmSaveDialog;
    private DialogInterface.OnClickListener dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setYesNoOptions();
        clearTextDialog();

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getUid();
        notesImageStorege = FirebaseStorage.getInstance().getReference();

        // get reference to buttons from layout
        editText = (EditText) findViewById(R.id.journalEditTextId);
        imageView = (ImageView) findViewById(R.id.pictureEditPageImageViewId);
        cameraBtn = (ImageButton) findViewById(R.id.cameraEditPageImageButtonId);
        saveBtn = (Button) findViewById(R.id.editNoteSaveBtnId);

        // intialize progressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.profileEditSaveDialog));

        // set the popup message
        confirmSaveDialog = new AlertDialog.Builder(this);
        confirmSaveDialog.setMessage(getString(R.string.areYouSureDialogMessage))
                .setPositiveButton(getString(R.string.alertClearTextYesOption), dialog)
                .setNegativeButton(getString(R.string.alertClearTextNoOption), dialog);

        alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.alertClearTextDialogMessage)
                .setPositiveButton(R.string.alertClearTextYesOption, dialogClearText).
                setNegativeButton(R.string.alertClearTextNoOption,dialogClearText);

        // get information from previous activity
        oldText = getIntent().getStringExtra("KEY_TEXT");
        oldImageURL = getIntent().getStringExtra("KEY_PICTURE");
        postKey = getIntent().getStringExtra("KEY_POST");

        // display the information on new activity
        editText.setText(getIntent().getStringExtra("KEY_TEXT"));
        Glide.with(this)
                .load(getIntent().getStringExtra("KEY_PICTURE"))
                .centerCrop()
                .into(imageView);

        // buttons action listeners
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSaveDialog.show();
            }
        });
    }

    // open gallery code
    private void openGallery() {
        Intent galleryI = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryI, IMAGE_GALLERY_REQUEST_CODE);
    }

    // open camera to take picture
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
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

    // yes no options
    private void setYesNoOptions() {
        dialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        progressDialog.show();
                        if (!editText.getText().toString().equals("")) {
                            if (editText.getText().toString() != oldText) {
                                changeNoteText();
                            }

                            if (imageURI != null) {
                                modifyNoteImage();
                                deleteOldNotePicture();
                            }

                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.toastSave), Toast.LENGTH_SHORT).show();
                            break;
                        }
                        progressDialog.dismiss();
                        startActivity(new Intent(EditNoteActivity.this,
                                MainPageActivity.class));
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.noteModified), Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_journal_menu, menu);

        clearTextMenu = menu.findItem(R.id.clearTextEditJournalMenu);

        clearTextMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                alert.show();

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    // set dialog message for clearing text
    private void clearTextDialog() {
        dialogClearText = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // get the text from the canvas first
                displayText = editText.getText().toString();

                // set yes or no behaviour for options
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        displayText = "";
                        editText.setText(displayText);
                        editText.setHint(R.string.canvasHintText);

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        editText.setText(displayText);

                        break;
                }
            }
        };
    }

    // modify current note image
    private void modifyNoteImage() {

        StorageReference profilePicturePath = notesImageStorege
                .child("NotesPhotos")
                .child(imageURI.getLastPathSegment());
        profilePicturePath.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri downloadableURL = taskSnapshot.getDownloadUrl();

                // set another child to store username and profile picture
                DatabaseReference postRef = FirebaseDatabase.getInstance()
                        .getReference("Notes/" + currentUserId + "/" + postKey);

                // complete data for current user
                postRef.child("picture").setValue(downloadableURL.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditNoteActivity.this, "Fail",
                        Toast.LENGTH_SHORT).show();

            }
        });

    }

    // delete the old picture
    private void deleteOldNotePicture() {
        StorageReference storage = FirebaseStorage.getInstance()
                .getReferenceFromUrl(oldImageURL);

        storage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("TAGS", "PHOTO DELETED");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAGF", "FAIL");
            }
        });
    }

    // change the text from note
    private void changeNoteText() {
        DatabaseReference postRef = FirebaseDatabase.getInstance()
                .getReference("Notes/" + currentUserId + "/" + postKey);
        postRef.child("text").setValue(editText.getText().toString());
        Log.e("TAGCHANGED", "TEXT CHANGED");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            imageURI = data.getData();
            Glide.with(getApplicationContext())
                    .load(imageURI)
                    .centerCrop()
                    .into(imageView);
        }

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // display the picture from the storage
            imageURI = Uri.parse(mCurrentPhotoPath);
            Glide.with(getApplicationContext())
                    .load(imageURI)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
