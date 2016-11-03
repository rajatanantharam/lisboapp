package app.lisboa.lisboapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.lisboa.lisboapp.R;
import app.lisboa.lisboapp.model.Event;
import app.lisboa.lisboapp.utils.FundaTextView;


/**
 * Created by Rajat Anantharam on 01/11/16.
 */

public class MainActivity extends AppCompatActivity {

    private ListView eventListView;
    private List<Event> eventList;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDataBase;
    private FirebaseUser user;
    private HashMap<Event, String > eventMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("What's going on?");
        }

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signInAnonymously();
        mDataBase = FirebaseDatabase.getInstance().getReference();
        user = mFirebaseAuth.getCurrentUser();

        eventListView = (ListView)findViewById(R.id.eventList);
        eventList = new ArrayList<>();
        eventMap = new HashMap<>();

        final EventAdapter eventAdapter = new EventAdapter(this,R.layout.event_adapter,eventList, mFirebaseAuth.getCurrentUser());
        eventListView.setAdapter(eventAdapter);
        eventAdapter.setJoinedRoomListener(new OnJoinedRoomListener() {
            @Override
            public void onJoinedRoom(Event event, View eventView) {
                Button joinRoom = (Button) eventView.findViewById(R.id.joinRoom);
                FundaTextView location = (FundaTextView) eventView.findViewById(R.id.locationName);

                if(event.hostId.equalsIgnoreCase(user.getUid())) {
                    Toast.makeText(MainActivity.this, "You cannot attend the event you created", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference databaseReference = mDataBase.child("events").child(eventMap.get(event));

                if(event.attendees == null) {
                    event.attendees = new ArrayList<>();
                }

                int count = event.attendees!=null ? event.attendees.size() : 0 ;

                if(event.attendees.contains(user.getUid())) {
                    event.attendees.remove(user.getUid());
                    joinRoom.setSelected(false);
                    count--;
                } else {
                    event.attendees.add(user.getUid());
                    joinRoom.setSelected(true);
                    count++;
                }

                String locationName = "+ " + count + " others at "+ event.locationName;
                location.setText(locationName);

                HashMap<String, Object> update = new HashMap<>();
                update.put("attendees", event.attendees);
                databaseReference.updateChildren(update);
            }
        });

        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Event addedEvent = dataSnapshot.getValue(Event.class);
                eventList.add(addedEvent);
                eventMap.put(addedEvent, dataSnapshot.getKey());
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("events").addChildEventListener(eventListener);
    }

    public void createEvent(View view) {
        Intent intent = new Intent(MainActivity.this,NewEventActivity.class);
        assert user != null;
        intent.putExtra("user_id",user.getUid());
        startActivity(intent);
    }

    public void goToInfoActivity(View view) {
    }

    public void goToHelpActivity(View view) {
    }
}
