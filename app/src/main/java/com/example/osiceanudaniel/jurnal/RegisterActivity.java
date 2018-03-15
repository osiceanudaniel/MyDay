package com.example.osiceanudaniel.jurnal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

	private EditText usernameEditText;
	private EditText emailEditText;
	private EditText passEditText;

	private Button signupBtn;

	private FirebaseAuth authUser;
	private FirebaseAuth.AuthStateListener authUserListener;

	private DatabaseReference databaseReference;

	private ProgressDialog registrationProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);


		// getting the views from XML
		usernameEditText = (EditText) findViewById(R.id.registerUsernameTextField);
		emailEditText = (EditText) findViewById(R.id.registerEmailTextField);
		passEditText = (EditText) findViewById(R.id.registerPasswordTextField);

		signupBtn = (Button) findViewById(R.id.registerBtn);

		usernameEditText.setText("");
		emailEditText.setText("");
		passEditText.setText("");

		// get the instance of current user
		authUser = FirebaseAuth.getInstance();

		// save info about current user in the database in the child Users
		databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

		registrationProgress = new ProgressDialog(RegisterActivity.this);

		registrationProgress.setTitle(this.getString(R.string.registrationProgressTitle));
		registrationProgress.setMessage(this.getString(R.string.registrationProgressMessage));

		// listen if the user taps register button
		signupBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String username = usernameEditText.getText().toString();
				String email = emailEditText.getText().toString();
				String password = passEditText.getText().toString();

				// check if the user completed all fields
				if(checkEditText(username, email, password)) {

					registrationProgress.show();

					// register the user
					registerUser(username, email, password);
				} else {

					Toast.makeText(RegisterActivity.this, R.string.toastEmptyText, Toast.LENGTH_SHORT).show();
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

	// check if fields are not empty
	private boolean checkEditText(String username, String email, String password) {
		if(TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

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

					// get the id of the current user
					String currentUserID = authUser.getCurrentUser().getUid();

					// set another child to store username and profile picture
					DatabaseReference dataToCurrentUser = databaseReference.child(currentUserID);

					// complete data for current user
					dataToCurrentUser.child("username").setValue(username);
					dataToCurrentUser.child("image").setValue("picture");

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
}
