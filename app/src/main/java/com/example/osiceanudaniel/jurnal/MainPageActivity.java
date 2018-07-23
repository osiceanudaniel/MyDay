package com.example.osiceanudaniel.jurnal;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainPageActivity extends AppCompatActivity {

    public static final int ALARM_REQUEST = 729;

    private boolean notificationON;
    private int hour;
    private int minute;

	private TextView usernameText;
    private TextView noNotesTextView;

    private FirebaseAuth authUser;
    private FirebaseAuth.AuthStateListener authUserListener;
    private DatabaseReference mainDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private DatabaseReference notesDatabaseReference;

    private DatabaseReference profilePictureDatabaseReference;
    private CircleImageView profileImageImageView;
    private RecyclerView notesListRecyclerView;
    private TextView loadingText;

    private String userID;
    private String profilePicture = "image path";
    private String deletedImageURL = "";

    private static Uri imageURI;
    private ProgressBar pBar;
    private AlertDialog.Builder confirmDialog;
    private DialogInterface.OnClickListener dialogDelete;
    private android.support.v7.widget.Toolbar toolbar;
    private String newPostKey;

    private LinearLayoutManager orderedByDateAscendingLayout;
    private LinearLayoutManager orderedByDateDescendingLayout;

    private FirebaseRecyclerAdapter<Notes, NotesViewHolder> firebaseRecyclerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        loadSharedPreferences();
        checkNotificationStatus(savedInstanceState);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarId);
        this.setSupportActionBar(toolbar);

        setYesNoDeleteOptions();

        loadingText = (TextView) findViewById(R.id.loadingTextViewId);

        pBar = (ProgressBar) findViewById(R.id.progressBarId);
        pBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);

        // set the popup message
        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setMessage(getString(R.string.areYouSureDeleteMessage))
                .setPositiveButton(getString(R.string.alertClearTextYesOption), dialogDelete)
                .setNegativeButton(getString(R.string.alertClearTextNoOption), dialogDelete);

        Log.e("TASVJASVFJ", "ON CREATE DE LA MAIN APELAT");

        authUser = FirebaseAuth.getInstance();
        mainDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mainDatabaseReference.keepSynced(true);

        profileImageImageView = (CircleImageView) findViewById(R.id.circleImageViewMain);
        noNotesTextView = (TextView) findViewById(R.id.noNotesTextView);
        noNotesTextView.setText("");

        // get the recycler view reference
        notesListRecyclerView = (RecyclerView) findViewById(R.id.recycleViewMain);
        notesListRecyclerView.setHasFixedSize(true);

        orderedByDateAscendingLayout = new LinearLayoutManager(this);
        orderedByDateDescendingLayout = new LinearLayoutManager(this);
        orderedByDateDescendingLayout.setReverseLayout(true);
        orderedByDateDescendingLayout.setStackFromEnd(true);
        notesListRecyclerView.setLayoutManager(orderedByDateDescendingLayout);

		authUserListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				if(authUser.getCurrentUser() == null) {
					startActivity(new Intent(MainPageActivity.this, LoginActivity.class));
				} else {

                    notesDatabaseReference = FirebaseDatabase.getInstance().getReference()
                            .child("Notes")
                            .child(authUser.getUid());
                    notesDatabaseReference.keepSynced(true);

                    // end recycler view reference

                    usernameText = (TextView) findViewById(R.id.usernameMainActivity);
                    userID = authUser.getUid();
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
                            profilePicture = (String) dataSnapshot.getValue();
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

		// edit user profile
		profileImageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = (String) usernameText.getText();

                Intent editProfileIntent = new Intent(MainPageActivity.this,
                        ProfileEditActivity.class);
                editProfileIntent.putExtra("KEY_USERNAME", username);
                editProfileIntent.putExtra("KEY_PICUTRE", profilePicture);
                startActivity(editProfileIntent);
            }
        });

	}


    public static class NotesViewHolder extends RecyclerView.ViewHolder {

	    View myView;
	    ImageView picture;
	    ImageButton deleteBtn;
	    ImageButton editBtn;

        public NotesViewHolder(final View itemView) {
            super(itemView);

            myView = itemView;

            picture = itemView.findViewById(R.id.imageCardViewId);
            deleteBtn = itemView.findViewById(R.id.deleteNoteId);
            editBtn = itemView.findViewById(R.id.editNoteId);

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

        notesListRecyclerView.setLayoutManager(orderedByDateDescendingLayout);

		authUser.addAuthStateListener(authUserListener);

        if(authUser.getCurrentUser() != null) {
                DatabaseReference seconDb = mainDatabaseReference.child("Notes");
                seconDb.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild(authUser.getUid())) {
                                FirebaseRecyclerAdapter<Notes, NotesViewHolder> firebaseRecyclerAdapter =
                                        new FirebaseRecyclerAdapter<Notes, NotesViewHolder>(
                                                Notes.class,
                                                R.layout.journal_list_row,
                                                NotesViewHolder.class,
                                                notesDatabaseReference.orderByChild("date")
                                        ) {
                                            @Override
                                            protected void populateViewHolder(NotesViewHolder viewHolder, final Notes model, final int position) {
                                                final String post_key = getRef(position).getKey();

                                                viewHolder.setText(model.getText());
                                                viewHolder.setDate(model.getData());
                                                viewHolder.setImage(getApplicationContext(), model.getPicture());

                                                viewHolder.picture.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        DatabaseReference imgRef = notesDatabaseReference
                                                                .child(post_key + "/picture");
                                                        imgRef.keepSynced(true);

                                                        imgRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                String path = (String) dataSnapshot.getValue();

                                                                imageURI = Uri.parse(path);
                                                                showPictureInView();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }
                                                });

                                                viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        confirmDialog.show();
                                                        newPostKey = post_key;
                                                    }
                                                });

                                                viewHolder.editBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        editNote(model.getText(), model.getPicture() , post_key);
                                                    }
                                                });
                                            }
                                        };
                                notesListRecyclerView.setAdapter(firebaseRecyclerAdapter);
                                noNotesTextView.setText("");

                                pBar.setVisibility(View.INVISIBLE);
                                loadingText.setVisibility(View.INVISIBLE);
                            } else {
                                noNotesTextView.setText(getString(R.string.noNotesText));
                                notesListRecyclerView.setClickable(false);
                                notesListRecyclerView.setLayoutFrozen(true);
                                pBar.setVisibility(View.INVISIBLE);
                                loadingText.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            noNotesTextView.setText(getString(R.string.noNotesText));
                            notesListRecyclerView.setClickable(false);
                            notesListRecyclerView.setLayoutFrozen(true);
                            pBar.setVisibility(View.INVISIBLE);
                            loadingText.setVisibility(View.INVISIBLE);
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
            case R.id.ascendingSort:
                notesListRecyclerView.setLayoutManager(orderedByDateAscendingLayout);
                break;
            case R.id.notificationMenu:
                startActivity(new Intent(MainPageActivity.this,
                        SetNotificationTimeActivity.class));
                break;
            case R.id.setPatternMenu:
                startActivity(new Intent(MainPageActivity.this,
                        SetPatternActivity.class));
                break;
            case R.id.aboutMenu:
                displayAboutText();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // set the behaviour of yes or no buttons from confirm dialog box
    private void setYesNoDeleteOptions() {
	    dialogDelete = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteNote(newPostKey);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
    }

    // delete selected note
    private void deleteNote(final String postKye) {
        final String currentUserId = authUser.getUid();
        DatabaseReference imageReference = FirebaseDatabase.getInstance()
                .getReference("Notes/" + currentUserId + "/" + postKye + "/picture");
        imageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deletedImageURL = (String) dataSnapshot.getValue();
                if (deletedImageURL != null) {
                    StorageReference storage = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(deletedImageURL);
                    storage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference keyReference = FirebaseDatabase.getInstance()
                                    .getReference("Notes/" + currentUserId + "/" + postKye);
                            keyReference.getRef().removeValue();
                            Toast.makeText(getApplicationContext(), getString(R.string.noteDeletedSuccess),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // edit selected note
    private void editNote(String text, String picture, String postKey) {
        Intent editIntent = new Intent(MainPageActivity.this,
                EditNoteActivity.class);
        editIntent.putExtra("KEY_TEXT", text);
        editIntent.putExtra("KEY_PICTURE", picture);
        editIntent.putExtra("KEY_POST", postKey);
        startActivity(editIntent);
    }

    // behaviour of back button
	public void onBackPressed(){
		Intent exit = new Intent(Intent.ACTION_MAIN);
		exit.addCategory(Intent.CATEGORY_HOME);
		exit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(exit);
	}

    // display the full size picture
    public void showPictureInView() {

        //Intent i=new Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(this, AUTHORITY, f));
//
//		i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//		startActivity(i);

        //
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageURI, "image/*");
        startActivity(intent);
    }

    //start the notification
    public void checkNotificationStatus(Bundle savedInstanceSta) {
	    if (notificationON) {
	        Log.e("TASAf", "NOTIFICATION IS BUILDING\n NOTIFICATION" +
                    ": " + hour + ":" + minute);
        } else {
            Log.e("TASAf", "NOTIFICATION ALREADY SET");
        }
        if (notificationON) {
            Calendar calendar = Calendar.getInstance();
            //calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 3);
            if (calendar.getTime().compareTo(new Date()) < 0) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            // set the notification
            Intent i = new Intent(getApplicationContext(), Notification_receiver.class);

            // alarm manager
            PendingIntent pI = PendingIntent.getBroadcast(getApplicationContext(),
                    ALARM_REQUEST, i, PendingIntent.FLAG_UPDATE_CURRENT);
           if (savedInstanceSta == null) {
               AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
               // set the repeating alarm
               alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, //triggered even in sleep mode
                       calendar.getTimeInMillis(), //when the alarm will be triggered
                       alarmManager.INTERVAL_DAY,  //notify every day
                       pI);
           }
        } else {
            try {
                Intent notificationIntent = new Intent(getApplicationContext(),
                        Notification_receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                        ALARM_REQUEST, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                        .getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(
                SetNotificationTimeActivity.SHARED_PREFERENCES, MODE_PRIVATE);

        hour = sharedPref.getInt(SetNotificationTimeActivity.HOUR_PREFERENCE, 18);
        minute = sharedPref.getInt(SetNotificationTimeActivity.MINUTE_PREFERENCE, 0);
        notificationON = sharedPref.getBoolean(SetNotificationTimeActivity.TOGGLE_STATE, true);
    }
}
