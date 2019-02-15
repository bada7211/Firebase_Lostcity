package com.example.firebasetest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
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
import java.util.Stack;

public class GestActivity extends AppCompatActivity implements GridAdapter.ListBtnClickListener {
    String room_name;
    FirebaseDatabase database;
    DatabaseReference roomDb;
    TextView round;
    GridView gridView;
    GridAdapter gridAdapter;
    ListView opntList_r,opntList_g,opntList_w,opntList_b,opntList_y;
    Button board_r,board_g,board_w,board_b,board_y;
    ListAdapter listAdapter;
    ListView curListView;
    ListView preListView;
    Button curBoard;
    Button preBoard;

    ArrayList<String> myDeck_list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gest);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        gridView = (GridView) findViewById(R.id.myDeckBoard);
        opntList_r =  (ListView) findViewById(R.id.rCard_opnt);
        opntList_g =  (ListView) findViewById(R.id.gCard_opnt);
        opntList_w =  (ListView) findViewById(R.id.wCard_opnt);
        opntList_b =  (ListView) findViewById(R.id.bCard_opnt);
        opntList_y =  (ListView) findViewById(R.id.yCard_opnt);
        board_r = (Button) findViewById(R.id.rBoard);
        board_g = (Button) findViewById(R.id.gBoard);
        board_w = (Button) findViewById(R.id.wBoard);
        board_b = (Button) findViewById(R.id.bBoard);
        board_y = (Button) findViewById(R.id.yBoard);

        round.setText("On");
        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference("RoomList");
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String game_state = dataSnapshot.getValue().toString();
                    if (game_state.equals("Start")) {
                        round.setText("Start");
                        updateMyDeck();
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference().child("RoomList");
                        stateDb.child(room_name).child("State").setValue("1ROUND");
                        round.setText("1ROUND");
                    }
                    if(game_state.contains("setDev")){
                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                        final String color = dataSnapshot.getValue().toString().substring(2,3);
                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host").child("DevCard").child(color);
                        devDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<String> opntDev_list = new ArrayList<String>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    opntDev_list.add(child.getValue().toString());
                                }
                                Collections.sort(opntDev_list);
                                findOpntCurList(color);
                                listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,opntDev_list,R.drawable.r_back);
                                curListView.setAdapter(listAdapter);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                    if(game_state.contains("setBoard")){
                        FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                        final String color = game_state.substring(2,3);
                        findOpntCurList(color);
                        DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name).child("Board").child(color);
                        boardDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    String card = child.getValue().toString();
                                    if(!(card.contains("1"))&&(card.contains("0"))) curBoard.setText("X");
                                    else curBoard.setText(card);
                                    break;
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
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
    public void onListBtnClick(int position, View v) {

    }

    public void findOpntCurList(String color) {
        if(color.equals("R")) {
            curListView = opntList_r;
            curBoard = board_r;
        }
        if(color.equals("G")) {
            curListView = opntList_g;
            curBoard = board_g;
        }
        if(color.equals("W")) {
            curListView = opntList_w;
            curBoard = board_w;
        }
        if(color.equals("B")) {
            curListView = opntList_b;
            curBoard = board_b;
        }
        if(color.equals("Y")) {
            curListView = opntList_y;
            curBoard = board_y;
        }
    }

    public void updateMyDeck() {
        FirebaseDatabase deck_base = FirebaseDatabase.getInstance();
        DatabaseReference deckDb = deck_base.getReference().child("RoomList").child(room_name).child("Gest").child("Card");
        deckDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDeck_list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    myDeck_list.add(child.getValue().toString());
                }
                gridAdapter = new GridAdapter(GestActivity.this,R.layout.my_deck,myDeck_list,GestActivity.this);
                gridView.setAdapter(gridAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
