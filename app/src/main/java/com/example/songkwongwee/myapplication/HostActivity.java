package com.example.songkwongwee.myapplication;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HostActivity extends AppCompatActivity {

    EditText editTextName;
    Button buttonAdd;
    Spinner spinnerSports;


    //our database reference object
    DatabaseReference databaseArtists;

    FirebaseAuth mAuth;

    ListView listViewArtists;

    //a list to store all the artist from firebase database
    List<Artist> artistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        //getting the reference of artists node
        databaseArtists = FirebaseDatabase.getInstance().getReference("hostedevents");

        mAuth = FirebaseAuth.getInstance();

        //getting views
        editTextName = findViewById(R.id.editTextName);
        buttonAdd = findViewById(R.id.buttonAddArtist);
        spinnerSports = findViewById(R.id.spinnerSports);


        listViewArtists = findViewById(R.id.listViewArtists);

        artistList = new ArrayList<>();

        buttonAdd.setOnClickListener(new View.OnClickListener() {   //click button method for adding event
            @Override
            public void onClick(View v) {
                //calling the method addArtist()
                //the method is defined below
                //this method is actually performing the write operation
                addArtist();
            }
        });

        listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {  //method for user to call update box when long click event
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Artist artist = artistList.get(position);

                showUpdateDialog(artist.getUserId(), artist.getEventName());
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        databaseArtists.addValueEventListener(new ValueEventListener() { //executed every time we change anything in database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {   //contains all data in snapshot data object

                artistList.clear(); //clear artist list

                for(DataSnapshot artistSnapshot : dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);

                    artistList.add(artist); //every time new data entered, this method executes,fetch all artist from database
                }

                ArtistList adapter = new ArtistList(HostActivity.this, artistList);
                listViewArtists.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(final String userId, String eventName){  //creates pop-out dialog for user update input

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.update_dialog, null);

        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        final Spinner spinnerGenres = dialogView.findViewById(R.id.spinnerSports);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Updating Event " +eventName);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenres.getSelectedItem().toString();

                if (TextUtils.isEmpty(name)) {
                   editTextName.setError("Name required");
                   return;
                }

                updateArtist(userId, name, genre);

                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteArtist(userId);
            }
        });

    }

    private void deleteArtist(String userId) {
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("hostedevents").child(userId);

        drArtist.removeValue();

        Toast.makeText(this, "Event Deleted Successfully", Toast.LENGTH_LONG).show();
    }

    private boolean updateArtist(String id, String name, String genre){

        //String uid = mAuth.getUid(); //call uid method to get user id

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("hostedevents").child(id); //search ref in "artist" node, child of node

        Artist artist = new Artist(id, name, genre);

        databaseReference.setValue(artist);

        Toast.makeText(this, "Event Updated Successfully", Toast.LENGTH_LONG).show();

        return true;
    }


    private void addArtist(){   //method for user to create event
        String name = editTextName.getText().toString().trim();
        String genre = spinnerSports.getSelectedItem().toString();

        if(!TextUtils.isEmpty(name)){   //if name is not empty

            //String uid = mAuth.getUid(); //call uid method to get user id

            String id = databaseArtists.push().getKey();  //call id method to get random string key

            Artist artist = new Artist(id, name, genre); //call event method to

            databaseArtists/*.child(uid)*/.child(id).setValue(artist);  //method to store node in organise form (Event>UserID>>Details)

            Toast.makeText(this,"Event added",Toast.LENGTH_LONG).show();    //Show UI
        }
        else{   //if name is empty
            Toast.makeText(this,"You should enter a name", Toast.LENGTH_LONG).show();   //Show error UI
        }

    }
}

//Need figure a way to allow only own user created event the rights to delete only their own events.