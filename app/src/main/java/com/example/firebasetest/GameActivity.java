package com.example.firebasetest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
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

    ListView myList_r,myList_g,myList_w,myList_b,myList_y;
    ListAdapter listAdapter;
    Button board_r,board_g,board_w,board_b,board_y;
    Boolean my_tern = false;
    String my_state = "ready";
    String my_selCard = "";
    View preSelView;
    View curSelView;
    ListView curListView;
    ListView preListView;
    Button curBoard;
    Button preBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        room_name = intent.getStringExtra("RoomName");
        round = (TextView) findViewById(R.id.round);
        round.setText("READY");
        gridView = (GridView) findViewById(R.id.myDeckBoard);
        myList_r =  (ListView) findViewById(R.id.rCard_me);
        myList_g =  (ListView) findViewById(R.id.gCard_me);
        myList_w =  (ListView) findViewById(R.id.wCard_me);
        myList_b =  (ListView) findViewById(R.id.bCard_me);
        myList_y =  (ListView) findViewById(R.id.yCard_me);
        board_r = (Button) findViewById(R.id.rBoard);
        board_g = (Button) findViewById(R.id.gBoard);
        board_w = (Button) findViewById(R.id.wBoard);
        board_b = (Button) findViewById(R.id.bBoard);
        board_y = (Button) findViewById(R.id.yBoard);

        setDevClickListener();
        setBoardClickListener();

        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String game_state = dataSnapshot.getValue().toString();
                    //카드 나눠주기 시작
                    if (game_state.equals("On")) {
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
                    if(game_state.equals("1ROUND")){
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
            findMyCurList(Character.toString(my_selCard.charAt(0)));
            if(preListView!=null) preListView.setEnabled(false);
            if(preBoard!=null) preBoard.setEnabled(false);
            preListView = curListView;
            preBoard = curBoard;
            curListView.setEnabled(true);
            curBoard.setEnabled(true);
        }
    }

    public void setBoardClickListener(){
        View.OnClickListener board_click = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(my_state.equals("Selected")) {
                    curBoard.setText(my_selCard);
                    FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                    DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name);
                    final String color = Character.toString(my_selCard.charAt(0));
                    devDb.child("Board").child(color).push().setValue(my_selCard);
                    curSelView.setSelected(false);
                    curBoard.setEnabled(false);
                    my_state = "SetBoardCard";
                    roomDb.setValue("1H"+color+"setBoard");
                }
            }
        };
        board_r.setOnClickListener(board_click);
        board_g.setOnClickListener(board_click);
        board_w.setOnClickListener(board_click);
        board_b.setOnClickListener(board_click);
        board_y.setOnClickListener(board_click);
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
                                ArrayList<String> myDev_list = new ArrayList<String>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    myDev_list.add(child.getValue().toString());
                                }
                                Collections.sort(myDev_list);
                                listAdapter = new ListAdapter(GameActivity.this,R.layout.card_item,myDev_list,R.drawable.r_back);
                                curListView.setAdapter(listAdapter);
                                curSelView.setSelected(false);
                                curListView.setEnabled(false);
                                my_state = "SetDevCard";
                                roomDb.setValue("1H"+color+"setDev");
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
        myList_r.setOnTouchListener(dev_touch);
        myList_g.setOnTouchListener(dev_touch);
        myList_w.setOnTouchListener(dev_touch);
        myList_b.setOnTouchListener(dev_touch);
        myList_y.setOnTouchListener(dev_touch);
        myList_r.setEnabled(false);
        myList_g.setEnabled(false);
        myList_w.setEnabled(false);
        myList_b.setEnabled(false);
        myList_y.setEnabled(false);
    }

    public void findMyCurList(String color) {
        if(color.equals("R")) {
            curListView = myList_r;
            curBoard = board_r;
        }
        if(color.equals("G")) {
            curListView = myList_g;
            curBoard = board_g;
        }
        if(color.equals("W")) {
            curListView = myList_w;
            curBoard = board_w;
        }
        if(color.equals("B")) {
            curListView = myList_b;
            curBoard = board_b;
        }
        if(color.equals("Y")) {
            curListView = myList_y;
            curBoard = board_y;
        }
    }
}
