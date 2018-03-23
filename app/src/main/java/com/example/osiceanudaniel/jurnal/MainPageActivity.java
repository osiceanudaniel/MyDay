package com.example.osiceanudaniel.jurnal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainPageActivity extends AppCompatActivity {

	private TextView usernameText;

	private FirebaseAuth authUser;
	private FirebaseAuth.AuthStateListener authUserListener;
    private DatabaseReference mainDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference notesDatabaseReference;
    private DatabaseReference profilePictureDatabaseReference;

    private CircleImageView profileImageImageView;
    private RecyclerView notesListRecyclerView;

    private FirebaseRecyclerAdapter<Notes, NotesViewHolder> firebaseRecyclerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_page);

        authUser = FirebaseAuth.getInstance();
        mainDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // get the recycler view reference
        notesListRecyclerView = (RecyclerView) findViewById(R.id.recycleViewMain);
        notesListRecyclerView.setHasFixedSize(true);
        notesListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		authUserListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				if(authUser.getCurrentUser() == null) {
					startActivity(new Intent(MainPageActivity.this, LoginActivity.class));
				} else {
                    profileImageImageView = (CircleImageView) findViewById(R.id.circleImageViewMain);

                    notesDatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Notes")
                            .child(authUser.getUid());

                    // end recycler view reference

                    usernameText = (TextView) findViewById(R.id.usernameMainActivity);
                    String userID = authUser.getUid();
                    userDatabaseReference = FirebaseDatabase.getInstance().
                            getReference("Users/"+userID+"/username");
                    userDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String username = (String) dataSnapshot.getValue();
                            usernameText.setText(username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    profilePictureDatabaseReference = FirebaseDatabase.getInstance().
                            getReference("Users/"+userID+"/profilePicture");
                    profilePictureDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String profilePicture = (String) dataSnapshot.getValue();
                            Glide.with(getApplicationContext()).load(profilePicture)
                                    .into(profileImageImageView);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
			}
		};

	}

	public static class NotesViewHolder extends RecyclerView.ViewHolder {

	    View myView;

        public NotesViewHolder(View itemView) {
            super(itemView);

            myView = itemView;
        }

        public void setImage(Context context, String image) {
            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageCardViewId);
            Glide.with(context).load(image).centerCrop().into(imageView);
        }

        public void setText(String text) {
            TextView textTextView = (TextView) itemView.findViewById(R.id.textCardViewId);
            textTextView.setText(text);
        }

        public void setDate(String date) {
            TextView daTextView = (TextView) itemView.findViewById(R.id.dataCardViewTextId);
            daTextView.setText(date);
        }
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

        if(authUser.getCurrentUser() != null) {
                DatabaseReference seconDb = mainDatabaseReference.child("Notes");
                seconDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            FirebaseRecyclerAdapter<Notes, NotesViewHolder> firebaseRecyclerAdapter =
                                    new FirebaseRecyclerAdapter<Notes, NotesViewHolder>(
                                            Notes.class,
                                            R.layout.journal_list_row,
                                            NotesViewHolder.class,
                                            notesDatabaseReference
                                    ) {
                                        @Override
                                        protected void populateViewHolder(NotesViewHolder viewHolder, Notes model, int position) {
                                            viewHolder.setText(model.getText());
                                            viewHolder.setDate(model.getData());
                                            viewHolder.setImage(getApplicationContext(), model.getPicture());
                                        }
                                    };
                            notesListRecyclerView.setAdapter(firebaseRecyclerAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        }
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

	public void onBackPressed(){
		Intent exit = new Intent(Intent.ACTION_MAIN);
		exit.addCategory(Intent.CATEGORY_HOME);
		exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(exit);
	}
}
