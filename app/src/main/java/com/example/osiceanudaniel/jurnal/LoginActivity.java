package com.example.osiceanudaniel.jurnal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

	private EditText emailEditText;
	private EditText passEditText;

	private Button loginBtn;

	private TextView registerLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		emailEditText = (EditText) findViewById(R.id.loginEmailTextField);
		passEditText = (EditText) findViewById(R.id.loginPasswordTextField);

		loginBtn = (Button) findViewById(R.id.loginSignInBtn);

		registerLink = (TextView) findViewById(R.id.loginRegisterLinkText);

		registerLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
				registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(registerIntent);
			}
		});
	}
}
