package com.example.firebasetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements GridAdapter.ListBtnClickListener {
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
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        round.setText("READY");
        gridView = (GridView) findViewById(R.id.myDeckBoard);

        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getValue().toString().equals("On")) {
                        round.setText("On");
                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        DatabaseReference roomDb2 = database2.getReference("RoomList");
//                        Toast.makeText(GameActivity.this, "Start", Toast.LENGTH_SHORT).show();;
                        roomDb2.child(room_name).child("Game").child("DeckCount").setValue(44);
                        roomDb2.child(room_name).child("Game").child("Score").setValue("0 : 0");
                        roomDb2.child(room_name).child("Game").child("Round").setValue(1);
                        Card card = new Card();
                        for(int i=0; i<8; i++) {
                            myDeck_list.add(card.card_deck.peek());
                            roomDb2.child(room_name).child("Host").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                            roomDb2.child(room_name).child("Gest").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                        }
                        for(int i=1; i<45; i++) {
                            roomDb2.child(room_name).child("Game").child("Deck").child(""+i+"").setValue(card.card_deck.pop());
                        }

                        gridAdapter = new GridAdapter(GameActivity.this,R.layout.my_deck,myDeck_list,GameActivity.this);
                        gridView.setAdapter(gridAdapter);

                        round.setText("Start");
                        roomDb2.child(room_name).child("State").setValue("Start");
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
//        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
//        DatabaseReference roomDb2 = database2.getReference("RoomList");
//        roomDb2.child(room_name).removeValue();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference roomDb2 = database2.getReference("RoomList");
        roomDb2.child(room_name).removeValue();
        super.onBackPressed();
    }

    @Override
    public void onListBtnClick(int position) {

    }
}
