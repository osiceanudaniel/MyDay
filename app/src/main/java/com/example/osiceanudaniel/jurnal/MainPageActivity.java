package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainPageActivity extends AppCompatActivity {

	private FirebaseAuth authUser;

	private FirebaseAuth.AuthStateListener authUserListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);

		authUser = FirebaseAuth.getInstance();

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

	private void createNote() {
        startActivity(new Intent(MainPageActivity.this, JournalActivity.class));
    }

    private void displayAboutText() {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle(this.getString(R.string.aboutTitle));
        infoDialog.setMessage(this.getString(R.string.aboutText));
        infoDialog.show();
    }

	@Override
	protected void onStart() {
		super.onStart();

		authUser.addAuthStateListener(authUserListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainpage_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
            case R.id.logoutMenu:
                logoutUser();
                break;
            case R.id.createNoteMenu:
                createNote();
                break;
            case R.id.aboutMenu:
                displayAboutText();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
