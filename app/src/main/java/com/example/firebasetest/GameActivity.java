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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
    Button board_r,board_g,board_w,board_b,board_y,board_deck;
    Boolean my_tern = false;
    String my_state = "ready";
    String my_selCard = "";
    View preSelView;
    View curSelView;
    ListView curListView;
    ListView preListView;
    Button curBoard;
    Button preBoard;

    HashMap<String, Stack<String>> board_stack = new HashMap<String, Stack<String>>();

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
        board_deck = (Button) findViewById(R.id.deckBoard);

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
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference("RoomList");
//                        Toast.makeText(GameActivity.this, "Start", Toast.LENGTH_SHORT).show();;
                        stateDb.child(room_name).child("Game").child("DeckCount").setValue(44);
                        stateDb.child(room_name).child("Game").child("Score").setValue("0 : 0");
                        stateDb.child(room_name).child("Game").child("Round").setValue(1);
                        Card card = new Card();
                        for(int i=0; i<8; i++) {
                            stateDb.child(room_name).child("Host").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                            stateDb.child(room_name).child("Gest").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                        }
                        for(int i=1; i<45; i++) {
                            stateDb.child(room_name).child("Game").child("Deck").child(""+i+"").setValue(card.card_deck.pop());
                        }
                        updateMyDeck();
                        updateBoard();
                        round.setText("Start");
                        stateDb.child(room_name).child("State").setValue("Start");
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
            findMyCurList(my_selCard.substring(0,1));
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
                //보드에 카드쌓기
                if(my_state.contains("SetCard")) {
                    //TODO:: 보드에서 카드먹는거 구현
                    curBoard = (Button) v;
                    String color = findBoardColor(v.getId());
                    String card = board_stack.get(color).peek();
                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
                    boardDb.child("Host").child("Card").child(card).setValue(card);
                    boardDb.child("Board").setValue("Rmv"+card);
                    curBoard.setEnabled(false);
//                    boardDb.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            int i = 0;
//                            for (DataSnapshot child : dataSnapshot.getChildren()) {
//                                String card = child.getValue().toString();
//                                FirebaseDatabase addDeck_base = FirebaseDatabase.getInstance();
//                                DatabaseReference addDeckDb = addDeck_base.getReference().child("RoomList").child(room_name).child("Host").child("Card");
//                                addDeckDb.child(card).setValue(card);
//                                child.getRef().removeValue();
//                                curBoard.setEnabled(false);
//                                break;
//                            }
//                        }
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                        }
//                    });
                }
                if(my_state.equals("Selected")) {
                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
                    boardDb.child("Board").setValue("Add"+my_selCard);
                    boardDb.child("Host").child("Card").child(my_selCard).removeValue();
                    curSelView.setSelected(false);
                    setBoardEnable(true);
                    curBoard.setEnabled(false);
                    my_state = "SetCardBoard";
                    roomDb.setValue("1H"+"setBoard");
//                    if(!(my_selCard.contains("1")) && (my_selCard.contains("0"))) curBoard.setText("X");
//                    else curBoard.setText(my_selCard);
//                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
//                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
//                    final String color = my_selCard.substring(0,1);
//                    boardDb.child("Host").child("Card").child(my_selCard).removeValue();
//                    boardDb.child("Board").child(color).push().setValue(my_selCard);
//                    curSelView.setSelected(false);
//                    setBoardEnable(true);
//                    curBoard.setEnabled(false);
//                    my_state = "SetCardBoard";
//                    roomDb.setValue("1H"+color+"setBoard");
                }
            }
        };
        board_r.setOnClickListener(board_click);
        board_g.setOnClickListener(board_click);
        board_w.setOnClickListener(board_click);
        board_b.setOnClickListener(board_click);
        board_y.setOnClickListener(board_click);
    }

    public void setDeckClickListener(){
        //TODO:: 덱클릭하면 먹는거 구현해야돼
    }

    public void setDevClickListener(){
        View.OnTouchListener dev_touch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(my_state.equals("Selected")) {
                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host");
                        final String color = my_selCard.substring(0,1);
                        devDb.child("Card").child(my_selCard).removeValue();
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
                                my_state = "SetCardDev";
                                roomDb.setValue("1H"+color+"setDev");
                                setBoardEnable(true);
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

    public  String findBoardColor(int id) {
        if(id == R.id.rBoard) return "R";
        if(id == R.id.gBoard) return "G";
        if(id == R.id.wBoard) return "W";
        if(id == R.id.bBoard) return "B";
        else  return "Y";
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
        DatabaseReference deckDb = deck_base.getReference().child("RoomList").child(room_name).child("Host").child("Card");
        deckDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDeck_list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    myDeck_list.add(child.getValue().toString());
                }
                gridAdapter = new GridAdapter(GameActivity.this,R.layout.my_deck,myDeck_list,GameActivity.this);
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
        upboardDb.setValue("Empty");
        upboardDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String board_state = dataSnapshot.getValue().toString();
                if(!(board_state.equals("Empty"))) {
                    final String color = board_state.substring(3, 4);
                    findMyCurList(color);
                    if (board_state.contains("Add")) {
                        String card = board_state.substring(3);
                        board_stack.get(color).push(card);
                        if (!(card.contains("1")) && (card.contains("0"))) curBoard.setText("X");
                        else curBoard.setText(card);
                    } else {
                        String card = board_stack.get(color).peek();
                        if (board_stack.get(color).isEmpty()) curBoard.setText("");
                        else {
                            if (!(card.contains("1")) && (card.contains("0")))
                                curBoard.setText("X");
                            else curBoard.setText(card);
                            board_stack.get(color).pop();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
