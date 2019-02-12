package com.example.firebasetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GestActivity extends AppCompatActivity {
    String room_name;
    FirebaseDatabase database;
    DatabaseReference roomDb;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gest);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        TextView round = (TextView) findViewById(R.id.round);
        round.setText("On");
        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference("RoomList");
        //TODO:: 노트참고
//        roomDb.set
//        roomDb.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                room_list.clear();
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    Map<String, String> map = (Map<String, String>) child.getValue();
//                    HashMap<String, String> rooms = new HashMap<>();
//                    rooms.put("Name", map.get("Name"));
//                    rooms.put("State", map.get("State"));
//                    room_list.add(rooms);
//                }
//                roomView.setAdapter(new SimpleAdapter(MainActivity.this, room_list, R.layout.room_item,
//                        new String[]{"Name", "State"},
//                        new int[]{R.id.roomName, R.id.roomState}));
//                roomView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        String name = room_list.get(position).get("Name");
//                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
//                        DatabaseReference roomDb2 = database2.getReference("RoomList");
//                        roomDb2.child(name).child("State").setValue("Start");
//                        Intent intent = new Intent(MainActivity.this, GestActivity.class);
//                        intent.putExtra("RoomName", name);
//                        startActivityForResult(intent, 2);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
    }

    @Override protected void onDestroy() {
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference roomDb2 = database2.getReference("RoomList");
        roomDb2.child(room_name).child("State").setValue("Ready");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference roomDb2 = database2.getReference("RoomList");
        roomDb2.child(room_name).child("State").setValue("Ready");
        super.onBackPressed();
    }

}
