package com.example.firebasetest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GestActivity extends AppCompatActivity implements GridAdapter.ListBtnClickListener {
    String room_name;
    FirebaseDatabase database;
    DatabaseReference roomDb;
    TextView round;
    GridView gridView;
    GridAdapter gridAdapter;
    ArrayList<String> myDeck_list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gest);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        gridView = (GridView) findViewById(R.id.myDeckBoard);
        round.setText("On");
        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference("RoomList");
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getValue().toString().equals("Start")) {
                        round.setText("Start");
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference roomDb2 = database2.getReference().child("RoomList").child(room_name).child("Gest").child("Card");
                        roomDb2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    myDeck_list.add(child.getValue().toString());
                                }
                                Collections.sort(myDeck_list);
                                gridAdapter = new GridAdapter(GestActivity.this,R.layout.my_deck,myDeck_list,GestActivity.this);
                                gridView.setAdapter(gridAdapter);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        roomDb2 = database2.getReference().child("RoomList");
                        roomDb2.child(room_name).child("State").setValue("1ROUND");
                        round.setText("1ROUND");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
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

    @Override
    public void onListBtnClick(int position, View v, String card_name) {

    }
}
