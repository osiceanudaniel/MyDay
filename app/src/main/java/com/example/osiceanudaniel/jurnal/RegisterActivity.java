package com.example.osiceanudaniel.jurnal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {

    // settin the request code for image request
    private final static int IMGE_REQUEST_CODE = 10;

    private Uri imageUri;
    private Uri cropedPictureUri = null;

	private EditText usernameEditText;
	private EditText emailEditText;
	private EditText passEditText;
	private EditText retypePassEditText;

	private Button signupBtn;

	private FirebaseAuth authUser;
	private FirebaseAuth.AuthStateListener authUserListener;

	private DatabaseReference databaseReference;

	private ProgressDialog registrationProgress;

	private ImageView profileImage;

	private StorageReference profileImageStorageReference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);


		// getting the views from XML
		usernameEditText = (EditText) findViewById(R.id.registerUsernameTextField);
		emailEditText = (EditText) findViewById(R.id.registerEmailTextField);
		passEditText = (EditText) findViewById(R.id.registerPasswordTextField);
		retypePassEditText = (EditText) findViewById(R.id.registerPasswordValidationTextField);

		signupBtn = (Button) findViewById(R.id.registerBtn);

		profileImage = (ImageView) findViewById(R.id.circleImageViewRegister);

		usernameEditText.setText("");
		emailEditText.setText("");
		passEditText.setText("");
		retypePassEditText.setText("");

		// get the instance of current user
		authUser = FirebaseAuth.getInstance();

		// save info about current user in the database in the child Users
		databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

		profileImageStorageReference = FirebaseStorage.getInstance().getReference();

		registrationProgress = new ProgressDialog(RegisterActivity.this);

		registrationProgress.setTitle(this.getString(R.string.registrationProgressTitle));
		registrationProgress.setMessage(this.getString(R.string.registrationProgressMessage));

		// setting the listener for profile image view
		profileImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent imageI = new Intent(Intent.ACTION_GET_CONTENT);
				// can choose an image with any extension
                imageI.setType("image/*");
				startActivityForResult(imageI, IMGE_REQUEST_CODE);
			}
		});

		// listen if the user taps register button
		signupBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String username = usernameEditText.getText().toString();
				String email = emailEditText.getText().toString();
				String password = passEditText.getText().toString();

				// check if the user completed all fields
				if(checkEditText(username, email, password, cropedPictureUri)) {

					registrationProgress.show();

					// register the user
					registerUser(username, email, password);
				} else {

					//Toast.makeText(RegisterActivity.this, R.string.toastEmptyText, Toast.LENGTH_SHORT).show();
				}
			}
		});

		authUserListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				// if user is logged in, log him out and go to the login page
				if(firebaseAuth.getCurrentUser() != null) {

					// go to main activity
					Intent loginIntent = new Intent(RegisterActivity.this, MainPageActivity.class);
					// check if activity login was already running
					loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(loginIntent);
				}
			}
		};
	}

	// override this method to get the image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check the request
        if(requestCode == IMGE_REQUEST_CODE && resultCode == RESULT_OK) {
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
                profileImage.setImageURI(cropedPictureUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    // check if fields are not empty
	private boolean checkEditText(String username, String email, String password, Uri image) {
		if(TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, R.string.toastEmptyText, Toast.LENGTH_SHORT).
                    show();
            return false;
		}

        if (checkPassMatch() == false) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.toastPasswordDontMatch),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (image == null) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.toastSelectAProfilePicture),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), this.getString(R.string.toastPasswordTooShort),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

		return true;
	}

	// try to register the user
	private void registerUser(final String username, String email, String password) {

		authUser.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {

				// if the registration was successful
				if(task.isSuccessful()) {

					// storage reference
                    StorageReference profilePicturePath = profileImageStorageReference
                            .child("ProfilePictures")
                            .child(cropedPictureUri.getLastPathSegment());
                    profilePicturePath.putFile(cropedPictureUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri downloadableURL = taskSnapshot.getDownloadUrl();

                            // get the id of the current user
                            String currentUserID = authUser.getCurrentUser().getUid();

                            // set another child to store username and profile picture
                            DatabaseReference dataToCurrentUser = databaseReference.child(currentUserID);

                            // complete data for current user
                            dataToCurrentUser.child("username").setValue(username);
                            dataToCurrentUser.child("profilePicture").setValue(downloadableURL.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Nu vrea", Toast.LENGTH_SHORT).show();
                        }
                    });

					Toast.makeText(RegisterActivity.this, R.string.toastSuccReg, Toast.LENGTH_SHORT).show();

					// dismiss the progress bar
					registrationProgress.dismiss();
				} else {

					// dismiss the progress bar
					registrationProgress.dismiss();

					Toast.makeText(RegisterActivity.this, R.string.registrationFailed, Toast.LENGTH_LONG).show();
				}

			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {

				Toast.makeText(RegisterActivity.this, R.string.registrationFailureText, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		// attach the listener to the auth object
		authUser.addAuthStateListener(authUserListener);
	}

	// check if passwords match
	private boolean checkPassMatch() {
	    String firstPass = passEditText.getText().toString();
	    String secondPass = retypePassEditText.getText().toString();

	    return firstPass.equals(secondPass);
    }
}
