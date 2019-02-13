package com.example.firebasetest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    ListView listView_r,listView_g,listView_w,listView_b,listView_y;
    ListAdapter listAdapter;
    ArrayList<String> myDev_list = new ArrayList<String>();

    Boolean my_tern = false;
    String my_state = "ready";
    String my_selCard = "";
    View preSelView;
    View curSelView;
    ListView curListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        round.setText("READY");
        gridView = (GridView) findViewById(R.id.myDeckBoard);
        listView_r =  (ListView) findViewById(R.id.rCard_me);
        listView_g =  (ListView) findViewById(R.id.gCard_me);
        listView_w =  (ListView) findViewById(R.id.wCard_me);
        listView_b =  (ListView) findViewById(R.id.bCard_me);
        listView_y =  (ListView) findViewById(R.id.yCard_me);

        setDevClickListener();

        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //카드 나눠주기 시작
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
                        Collections.sort(myDeck_list);
                        gridAdapter = new GridAdapter(GameActivity.this,R.layout.my_deck,myDeck_list,GameActivity.this);
                        gridView.setAdapter(gridAdapter);

                        round.setText("Start");
                        roomDb2.child(room_name).child("State").setValue("Start");
                    }
                    //1라운드 호스트 카드버리기 턴
                    if(dataSnapshot.getValue().toString().equals("1ROUND")){
                        my_state = "SelCard";
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
    public void onListBtnClick(int position, View v) {
        if(my_state.equals("SelCard")||my_state.equals("Selected")) {
            if (curSelView == null) {
                ((Button) v).setSelected(true);
                curSelView = v;
            } else {
                ((Button) v).setSelected(true);
                preSelView = curSelView;
                curSelView = v;
                if (curSelView != preSelView) ((Button) preSelView).setSelected(false);
            }
            my_selCard = myDeck_list.get(position);
            my_state = "Selected";
            if((my_selCard.contains("R"))) {
                curListView = listView_r;
                listView_r.setEnabled(true);
            }
            if((my_selCard.contains("G"))) {
                curListView = listView_g;
                listView_g.setEnabled(true);
            }
            if((my_selCard.contains("W"))) {
                curListView = listView_w;
                listView_w.setEnabled(true);
            }
            if((my_selCard.contains("B"))) {
                curListView = listView_b;
                listView_b.setEnabled(true);
            }
            if((my_selCard.contains("Y"))) {
                curListView = listView_y;
                listView_y.setEnabled(true);
            }
        }
    }

    public void setDevClickListener(){
        View.OnTouchListener dev_touch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(my_state.equals("Selected")) {
                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host");
                        final String color = Character.toString(my_selCard.charAt(0));
                        devDb.child("DevCard").child(color).child(my_selCard).setValue(my_selCard);
                        devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host").child("DevCard").child(color);
                        devDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                myDev_list.clear();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    myDev_list.add(child.getValue().toString());
                                }
                                Collections.sort(myDev_list);
                                if(color.equals("R")) listAdapter = new ListAdapter(GameActivity.this,R.layout.card_item,myDev_list,R.drawable.r_back);
                                else listAdapter = new ListAdapter(GameActivity.this,R.layout.card_item2,myDev_list,R.drawable.r_back);
                                curListView.setAdapter(listAdapter);
                                curSelView.setSelected(false);
                                curListView.setEnabled(false);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
                return false;
            }
        };
        listView_r.setOnTouchListener(dev_touch);
        listView_g.setOnTouchListener(dev_touch);
        listView_w.setOnTouchListener(dev_touch);
        listView_b.setOnTouchListener(dev_touch);
        listView_y.setOnTouchListener(dev_touch);
        listView_r.setEnabled(false);
        listView_g.setEnabled(false);
        listView_w.setEnabled(false);
        listView_b.setEnabled(false);
        listView_y.setEnabled(false);
    }
}
