package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainPageActivity extends AppCompatActivity {

	private Button logoutBtn;

	private FirebaseAuth authUser;

	private FirebaseAuth.AuthStateListener authUserListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);

		//

		Button journalBtn;
		journalBtn = (Button) findViewById(R.id.journalBtn);

		journalBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainPageActivity.this, JournalActivity.class));
			}
		});

		//

		logoutBtn = (Button) findViewById(R.id.logoutBtn);

		authUser = FirebaseAuth.getInstance();

		logoutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				logoutUser();
			}
		});

		authUserListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				if(firebaseAuth.getCurrentUser() == null) {
					startActivity(new Intent(MainPageActivity.this, LoginActivity.class));
				}
			}
		};
	}

	private void logoutUser() {

		authUser.signOut();
	}

	@Override
	protected void onStart() {
		super.onStart();

		authUser.addAuthStateListener(authUserListener);
	}
}
