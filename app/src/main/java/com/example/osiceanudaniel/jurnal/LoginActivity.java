package com.example.osiceanudaniel.jurnal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

	private EditText emailEditText;
	private EditText passEditText;

	private Button loginBtn;

	private TextView registerLink;

	private FirebaseAuth authUser;

	private ProgressDialog progressLogin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		emailEditText = (EditText) findViewById(R.id.loginEmailTextField);
		passEditText = (EditText) findViewById(R.id.loginPasswordTextField);

		loginBtn = (Button) findViewById(R.id.loginSignInBtn);

		registerLink = (TextView) findViewById(R.id.loginRegisterLinkText);

		authUser = FirebaseAuth.getInstance();

		emailEditText.setText("");
		passEditText.setText("");

		progressLogin = new ProgressDialog(LoginActivity.this);
		progressLogin.setTitle(R.string.loginProgressTitle);

		// listen if the user taps the register text
		registerLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// start the register activity
				Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
				// se if the activity was already open
				registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(registerIntent);
			}
		});

		//listen if the user taps sign in button
		loginBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String email = emailEditText.getText().toString();
				String password = passEditText.getText().toString();

				// check if the user entered somethig
				if (checkEditText(email, password)) {

					progressLogin.show();

					// login the user
					loginUser(email, password);
				} else {
					Toast.makeText(LoginActivity.this, R.string.toastEmptyText, Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	// check if email and password fields are empty
	private boolean checkEditText(String email, String password) {
		if(email.equals("") || password.equals("")) {
			return false;
		}

		return true;
	}

	// login the user and redirect him to the main page
	private void loginUser(String email, String password) {

		authUser.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {

				// if login was successful
				if(task.isSuccessful()) {

					// redirect the user
					Intent mainPageIntent = new Intent(LoginActivity.this, MainPageActivity.class);
					mainPageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(mainPageIntent);

					// dismiss the progress bar
					progressLogin.dismiss();
				} else {

					// dismiss the progress bar
					progressLogin.dismiss();
					passEditText.setText("");

					Toast.makeText(LoginActivity.this, R.string.loginFailed, Toast.LENGTH_LONG).show();
				}

			}
		});
	}

    public void onBackPressed(){
        Intent exit = new Intent(Intent.ACTION_MAIN);
        exit.addCategory(Intent.CATEGORY_HOME);
        exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(exit);
    }


}
