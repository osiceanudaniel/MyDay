package com.example.osiceanudaniel.jurnal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity {

    public static final int IMAGE_GALLERY_REQUEST_CODE = 80;

    private EditText usernameEdit;
    private CircleImageView profilePicture;
    private Button saveBtn;
    private Button chooseFileBtn;

    private DatabaseReference userDbReference;
    private StorageReference profileImageStorageReference;
    private FirebaseAuth currentUser;
    private String userId;
    private String newUsername;
    private String oldUsername;
    private Uri imageUri = null;
    private Uri cropedPictureUri = null;
    private String profilePictureURL;

    private ProgressDialog progressDialog;
    private ProgressBar imageLoadingProgress;
    private AlertDialog.Builder areYouSureDialog;
    private DialogInterface.OnClickListener dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setYesNoOptionsBehaviour();

        // user auth
        currentUser = FirebaseAuth.getInstance();
        userId = currentUser.getUid();

        // db user reference
        userDbReference = FirebaseDatabase.getInstance().getReference("Users/" + userId);
        profileImageStorageReference = FirebaseStorage.getInstance().getReference();

        imageLoadingProgress = (ProgressBar) findViewById(R.id.profileEditProgressBarId);
        imageLoadingProgress.setVisibility(View.VISIBLE);

        usernameEdit = (EditText) findViewById(R.id.newUsernameEditTextId);
        profilePicture = (CircleImageView) findViewById(R.id.newProfilePictureId);
        saveBtn = (Button) findViewById(R.id.saveNewProfileBtnId);
        chooseFileBtn = (Button) findViewById(R.id.changePictureBtnId);
        progressDialog = new ProgressDialog(this);

        // set the popup message
        areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setMessage(getString(R.string.areYouSureDialogMessage))
                .setPositiveButton(getString(R.string.alertClearTextYesOption), dialog)
                .setNegativeButton(getString(R.string.alertClearTextNoOption), dialog);

        // set the progress bar
        progressDialog.setTitle(getString(R.string.profileEditSaveDialog));

        // get the data from previous activity
        usernameEdit.setText(getIntent().getStringExtra("KEY_USERNAME"));
        oldUsername = getIntent().getStringExtra("KEY_USERNAME");
        //set the cursor at the end of text
        usernameEdit.setSelection(usernameEdit.getText().length());
        Log.e("YFAGSFK", "Picture path: " + getIntent().getStringExtra("KEY_PICUTRE"));
        Glide.with(getApplicationContext()).load(getIntent().getStringExtra("KEY_PICUTRE"))
                .into(profilePicture);
        profilePictureURL = getIntent().getStringExtra("KEY_PICUTRE");

        // attach a listener on save button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newUsername = usernameEdit.getText().toString();
                if (!newUsername.equals("")) {
                    areYouSureDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.enterUsername),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // attach listener on choose file button
        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        imageLoadingProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check the request
        if(requestCode == IMAGE_GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            // get the image path
            imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setBorderLineColor(Color.RED)
                    .setGuidelinesColor(Color.GREEN)
                    .setFixAspectRatio(true)
                    .setBackgroundColor(Color.parseColor("#8010b6cd"))
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropedPictureUri = result.getUri();

                // set the profile picture to be the cropped image
                profilePicture.setImageURI(cropedPictureUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    // set the action of yes and no buttons
    private void setYesNoOptionsBehaviour() {
        dialog = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                newUsername = usernameEdit.getText().toString();
                DatabaseReference usernameDbRef = userDbReference.child("username");

                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (newUsername != oldUsername) {
                            usernameDbRef.setValue(newUsername);
                        }

                        if (cropedPictureUri != null) {
                            insertNewPhoto();
                            deleteOldPhoto();
                        }
                        progressDialog.dismiss();
                        startActivity(new Intent(ProfileEditActivity.this,
                                MainPageActivity.class));
                        Toast.makeText(getApplicationContext(), getString(R.string.profileEdited),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
    }

    // open image gallery
    private void openGallery() {
        Intent galleryI = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galleryI, IMAGE_GALLERY_REQUEST_CODE);
    }

//    // delete the old profile picture
//    private void getProfileImageRefFromDatabase() {
//        final DatabaseReference pictureReference = userDbReference.child("profilePicture");
//        pictureReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                profilePictureURL = dataSnapshot.getValue().toString();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }

    // delete old photo
    private void deleteOldPhoto() {
        StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(profilePictureURL);

        storage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("TATAassa", "PHOTO DELETED");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TATAassaaagsgas", "A INTRAT PE FAILURE LA DELETE");
            }
        });
    }

    // insert new profile picture
    private void insertNewPhoto() {
        StorageReference profilePicturePath = profileImageStorageReference
                .child("ProfilePictures")
                .child(cropedPictureUri.getLastPathSegment());
        profilePicturePath.putFile(cropedPictureUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Uri downloadableURL = taskSnapshot.getDownloadUrl();

                // get the id of the current user
                String currentUserID = currentUser.getCurrentUser().getUid();

                // set another child to store username and profile picture
                DatabaseReference dataToCurrentUser = FirebaseDatabase.getInstance()
                        .getReference("Users/" + currentUserID);

                // complete data for current user
                dataToCurrentUser.child("profilePicture").setValue(downloadableURL.toString());
                //deleteOldPhoto();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileEditActivity.this, "Nu vrea",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }
}
