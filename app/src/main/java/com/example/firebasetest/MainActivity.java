package com.example.firebasetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String,String>> room_list = new ArrayList<HashMap<String,String>>();
    ListView roomView;
    FirebaseDatabase database;
    DatabaseReference roomDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roomView = (ListView) findViewById(R.id.roomList);

        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference("RoomList");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                room_list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String, String> map = (Map<String, String>) child.getValue();
                    HashMap<String, String> rooms = new HashMap<>();
                    rooms.put("Name", map.get("Name"));
                    rooms.put("State", map.get("State"));
                    room_list.add(rooms);
                }
                roomView.setAdapter(new SimpleAdapter(MainActivity.this, room_list, R.layout.room_item,
                        new String[]{"Name", "State"},
                        new int[]{R.id.roomName, R.id.roomState}));
                roomView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = room_list.get(position).get("Name");
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference roomDb2 = database2.getReference("RoomList");
                        roomDb2.child(name).child("State").setValue("On");
                        Intent intent = new Intent(MainActivity.this, GestActivity.class);
                        intent.putExtra("RoomName", name);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });





//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
//                String name = dataSnapshot.getKey();
//                Map<String,String> map = (Map<String,String>) dataSnapshot.getValue();
//                HashMap<String,String> rooms = new HashMap<>();
//                rooms.put("Name",name);
//                rooms.put("State",map.get("State"));
//                room_list.add(rooms);
//                roomView.setAdapter(new SimpleAdapter(MainActivity.this,room_list,R.layout.room_item,
//                        new String[] {"Name","State"},
//                        new int[] {R.id.roomName,R.id.roomState}));
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        };
//        roomDb.addChildEventListener(childEventListener);
    }

    public void createRoom_Start(View v) {
        Intent intent = new Intent(this, CreateRoomActivity.class);
        intent.putExtra("RoomCreateMsg", "방 만들기");
        startActivityForResult(intent,1);
    }
}
