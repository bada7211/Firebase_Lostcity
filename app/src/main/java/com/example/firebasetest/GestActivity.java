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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GestActivity extends AppCompatActivity implements GridAdapter.ListBtnClickListener {
    String room_name;
    FirebaseDatabase database;
    DatabaseReference roomDb;
    TextView round;
    GridView gridView;
    GridAdapter gridAdapter;
    ListView myList_r,myList_g,myList_w,myList_b,myList_y;
    ListView opntList_r,opntList_g,opntList_w,opntList_b,opntList_y;
    Button board_r,board_g,board_w,board_b,board_y,board_deck;
    ListAdapter listAdapter;
    ListView curListView;
    ListView preListView;
    Button curBoard;
    Button preBoard;
    String my_state = "ready";
    String my_selCard = "";
    View preSelView;
    View curSelView;

    HashMap<String, Stack<String>> board_stack = new HashMap<String, Stack<String>>();

    ArrayList<String> myDeck_list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gest);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        gridView = (GridView) findViewById(R.id.myDeckBoard);
        myList_r =  (ListView) findViewById(R.id.rCard_me);
        myList_g =  (ListView) findViewById(R.id.gCard_me);
        myList_w =  (ListView) findViewById(R.id.wCard_me);
        myList_b =  (ListView) findViewById(R.id.bCard_me);
        myList_y =  (ListView) findViewById(R.id.yCard_me);
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
        board_deck = (Button) findViewById(R.id.deckBoard);

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
                        updateBoard();
                        updateDevList();
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference().child("RoomList");
                        stateDb.child(room_name).child("State").setValue("1ROUND");
                        round.setText("1ROUND");
                    }
//                    if(game_state.contains("setDev")){
//                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
//                        final String color = dataSnapshot.getValue().toString().substring(2,3);
//                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host").child("DevCard").child(color);
//                        devDb.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                ArrayList<String> opntDev_list = new ArrayList<String>();
//                                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                                    opntDev_list.add(child.getValue().toString());
//                                }
//                                Collections.sort(opntDev_list);
//                                findCurList(color, false);
//                                listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,opntDev_list,R.drawable.r_back);
//                                curListView.setAdapter(listAdapter);
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                            }
//                        });
//                    }
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

    public void findCurList(String color,Boolean tern) {
        if(color.equals("R")) {
            if(tern) curListView = myList_r;
            else curListView = opntList_r;
            curBoard = board_r;
        }
        if(color.equals("G")) {
            if(tern) curListView = myList_g;
            else curListView = opntList_g;
            curBoard = board_g;
        }
        if(color.equals("W")) {
            if(tern) curListView = myList_w;
            else curListView = opntList_w;
            curBoard = board_w;
        }
        if(color.equals("B")) {
            if(tern) curListView = myList_b;
            else curListView = opntList_b;
            curBoard = board_b;
        }
        if(color.equals("Y")) {
            if(tern) curListView = myList_y;
            else curListView = opntList_y;
            curBoard = board_y;
        }
        if(color.equals("Clear")){
            curListView = null;
            curSelView = null;
            curBoard = null;
        }
    }

    public void setBoardEnable(Boolean set) {
        if(!(board_r.getText().equals(""))) board_r.setEnabled(set);
        if(!(board_g.getText().equals(""))) board_g.setEnabled(set);
        if(!(board_w.getText().equals(""))) board_w.setEnabled(set);
        if(!(board_b.getText().equals(""))) board_b.setEnabled(set);
        if(!(board_y.getText().equals(""))) board_y.setEnabled(set);
        board_deck.setEnabled(set);
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
    public void updateBoard() {
        board_stack.put("R",new Stack<String>());
        board_stack.put("G",new Stack<String>());
        board_stack.put("W",new Stack<String>());
        board_stack.put("B",new Stack<String>());
        board_stack.put("Y",new Stack<String>());
        FirebaseDatabase upboard_base = FirebaseDatabase.getInstance();
        DatabaseReference upboardDb = upboard_base.getReference().child("RoomList").child(room_name).child("Board");
        upboardDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String board_state = dataSnapshot.getValue().toString();
                    final String color = board_state.substring(3, 4);
                    findCurList(color, false);
                    if (board_state.contains("Add")) {
                        String card = board_state.substring(3);
                        board_stack.get(color).push(card);
                        if (!(card.contains("1")) && (card.contains("0")))
                            curBoard.setText("X");
                        else curBoard.setText(card);
                    }
                    else {
                        board_stack.get(color).pop();
                        if (board_stack.get(color).isEmpty()) curBoard.setText("");
                        else {
                            String card = board_stack.get(color).peek();
                            if (!(card.contains("1")) && (card.contains("0")))
                                curBoard.setText("X");
                            else curBoard.setText(card);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void updateDevList() {
        List<String> colors = Arrays.asList("R","G","W","B","Y");
        for(String color: colors) {
            FirebaseDatabase g_list_base = FirebaseDatabase.getInstance();
            DatabaseReference glistDb = g_list_base.getReference().child("RoomList").child(room_name).child("Gest").child("DevCard").child(color);
            glistDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        findCurList(dataSnapshot.getKey(),true);
                        ArrayList<String> myDev_list = new ArrayList<String>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            myDev_list.add(child.getValue().toString());
                        }
                        listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,myDev_list,R.drawable.r_back);
                        curListView.setAdapter(listAdapter);
                        curSelView.setSelected(false);
                        curListView.setEnabled(false);
                        my_state = "SetCardDev";
                        setBoardEnable(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            FirebaseDatabase h_list_base = FirebaseDatabase.getInstance();
            DatabaseReference hlistDb = h_list_base.getReference().child("RoomList").child(room_name).child("Host").child("DevCard").child(color);
            hlistDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        findCurList(dataSnapshot.getKey(),false);
                        ArrayList<String> opntDev_list = new ArrayList<String>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            opntDev_list.add(child.getValue().toString());
                        }
                        listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,opntDev_list,R.drawable.r_back);
                        curListView.setAdapter(listAdapter);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
