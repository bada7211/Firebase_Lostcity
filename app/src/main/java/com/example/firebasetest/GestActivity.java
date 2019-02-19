package com.example.firebasetest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
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
    int deck_count;
    int round_count = 0;
    int my_score = 0;
    int opnt_score = 0;

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

        setDevClickListener();
        setBoardClickListener();
        setDeckClickListener();

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
                        updateBoardDeck();
                        round_count = 1;
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference().child("RoomList");
                        stateDb.child(room_name).child("State").setValue("1ROUNDH");
                        round.setText("1ROUND");
                    }
                    //1라운드 게스트턴
                    if(game_state.contains("ROUNDG")){
                        findCurList("Clear",true);
                        my_state = "SelCard";
                    }
                    if(game_state.contains("END")){
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference("RoomList").child(room_name);
                        if(game_state.contains("3")) {
                            //TODO:: 게임종료
                        }
                        else{
                            resetGame();
                            round_count += 1;
                            round.setText(round_count + "ROUND");
                        }
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
            findCurList(my_selCard.substring(0,1),true);
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
                //보드에 카드쌓기,먹기
                if(my_state.contains("SetCard")) {
                    setBoardEnable(false);
                    curBoard = (Button) v;
                    String color = findBoardColor(v.getId());
                    String card = board_stack.get(color).peek();
                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
                    boardDb.child("Gest").child("Card").child(card).setValue(card);
                    boardDb.child("Board").setValue("Rmv"+card);
                    FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                    DatabaseReference stateDb = state_base.getReference().child("RoomList");
                    stateDb.child(room_name).child("State").setValue(round_count+"ROUNDH");
                    my_state = "Ready";
                }
                if(my_state.equals("Selected")) {
                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
                    boardDb.child("Board").setValue("Add"+my_selCard);
                    boardDb.child("Gest").child("Card").child(my_selCard).removeValue();
                    curSelView.setSelected(false);
                    setBoardEnable(true);
                    curBoard.setEnabled(false);
                    my_state = "SetCardBoard";
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
        View.OnClickListener boarddeck_click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (my_state.contains("SetCard")) {
                    setBoardEnable(false);
                    FirebaseDatabase mydeck_base = FirebaseDatabase.getInstance();
                    DatabaseReference mydeckDb = mydeck_base.getReference().child("RoomList").child(room_name).child("Game").child("Deck");
                    mydeckDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String card = dataSnapshot.child("" + deck_count + "").getValue().toString();
                            FirebaseDatabase getdeck_base = FirebaseDatabase.getInstance();
                            DatabaseReference getdeckDb = getdeck_base.getReference().child("RoomList").child(room_name);
                            getdeckDb.child("Gest").child("Card").child(card).setValue(card);
                            getdeckDb.child("Game").child("DeckCount").setValue(deck_count - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        };
        board_deck.setOnClickListener(boarddeck_click);
    }

    public void setDevClickListener(){
        View.OnTouchListener dev_touch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(my_state.equals("Selected")) {
                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Gest");
                        final String color = my_selCard.substring(0,1);
                        devDb.child("Card").child(my_selCard).removeValue();
                        devDb.child("DevCard").child(color).child(my_selCard).setValue(my_selCard);
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

    public  String findBoardColor(int id) {
        if(id == R.id.rBoard) return "R";
        if(id == R.id.gBoard) return "G";
        if(id == R.id.wBoard) return "W";
        if(id == R.id.bBoard) return "B";
        else  return "Y";
    }

    public TextView findCurScoreView(String color,Boolean flag) {
        if(flag){
            if(color.equals("R")) return (TextView)findViewById(R.id.rScore_me);
            else if(color.equals("G")) return (TextView)findViewById(R.id.gScore_me);
            else if(color.equals("W")) return (TextView)findViewById(R.id.wScore_me);
            else if(color.equals("B")) return (TextView)findViewById(R.id.bScore_me);
            else return (TextView)findViewById(R.id.yScore_me);
        }
        else{
            if(color.equals("R")) return (TextView)findViewById(R.id.rScore_opnt);
            else if(color.equals("G")) return (TextView)findViewById(R.id.gScore_opnt);
            else if(color.equals("W")) return (TextView)findViewById(R.id.wScore_opnt);
            else if(color.equals("B")) return (TextView)findViewById(R.id.bScore_opnt);
            else return (TextView)findViewById(R.id.yScore_opnt);
        }
    }

    public void setTotalScore(int score, int pre_score, Boolean flag) {
        if(flag) {
            my_score += (score-pre_score);
            TextView total = (TextView) findViewById(R.id.tScore_me);
            total.setText(String.valueOf(my_score));
        }
        else {
            opnt_score += (score-pre_score);
            TextView total = (TextView) findViewById(R.id.tScore_opnt);
            total.setText(String.valueOf(opnt_score));
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

    public  void updateBoardDeck() {
        FirebaseDatabase boarddeck_base = FirebaseDatabase.getInstance();
        DatabaseReference boarddeckDb = boarddeck_base.getReference().child("RoomList").child(room_name).child("Game").child("DeckCount");
        boarddeckDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    deck_count = Integer.parseInt(dataSnapshot.getValue().toString());
                    board_deck.setText(""+deck_count+"");
                    if(my_state.contains("SetCard")) {
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference().child("RoomList");
                        if(deck_count == 0) stateDb.child(room_name).child("State").setValue(round_count+"ROUNDEND");
                        else stateDb.child(room_name).child("State").setValue(round_count+"ROUNDH");
                        my_state = "Ready";
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
            final String s_color = color;
            FirebaseDatabase g_list_base = FirebaseDatabase.getInstance();
            DatabaseReference glistDb = g_list_base.getReference().child("RoomList").child(room_name).child("Gest").child("DevCard").child(color);
            glistDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        findCurList(dataSnapshot.getKey(),true);
                        ArrayList<String> myDev_list = new ArrayList<String>();
                        int score = 0;
                        int mult = 1;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String card = child.getValue().toString();
                            String num = card.substring(1);
                            if(!(num.contains("1"))&&(num.contains("0"))) mult += 1;
                            else score += Integer.parseInt(num);
                            myDev_list.add(card);
                        }
                        score = (score - 20) * mult;
                        int pre_score = Integer.parseInt(findCurScoreView(s_color,true).getText().toString());
                        findCurScoreView(s_color,true).setText(String.valueOf(score));
                        setTotalScore(score,pre_score,true);
                        listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,myDev_list);
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
                        int score = 0;
                        int mult = 1;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String card = child.getValue().toString();
                            String num = card.substring(1);
                            if(!(num.contains("1"))&&(num.contains("0"))) mult += 1;
                            else score += Integer.parseInt(num);
                            opntDev_list.add(card);
                        }
                        score = (score - 20) * mult;
                        int pre_score = Integer.parseInt(findCurScoreView(s_color,false).getText().toString());
                        findCurScoreView(s_color,false).setText(String.valueOf(score));
                        setTotalScore(score,pre_score,false);
                        listAdapter = new ListAdapter(GestActivity.this,R.layout.card_item,opntDev_list);
                        curListView.setAdapter(listAdapter);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
    public void resetGame() {
        board_stack.clear();
        opnt_score = 0;
        my_score = 0;
        TextView total = (TextView) findViewById(R.id.tScore_me);
        total.setText(String.valueOf(my_score));
        total = (TextView) findViewById(R.id.tScore_opnt);
        total.setText(String.valueOf(opnt_score));
        List<String> colors = Arrays.asList("R","G","W","B","Y");
        for(String color: colors) {
            board_stack.put(color,new Stack<String>());
            findCurScoreView(color,true).setText("0");
            findCurScoreView(color,false).setText("0");
        }

        //보드초기화
        Button[] boards = {board_r,board_g,board_w,board_b,board_y};
        for(Button board : boards) {
            board.setText("");
        }
        //리스트초기화
        ListView[] lists = {myList_r,myList_g,myList_w,myList_b,myList_y,
                opntList_r,opntList_g,opntList_w,opntList_b,opntList_y};
        for(ListView list : lists) {
            ArrayList<String> myDev_list = new ArrayList<String>();
            myDev_list.clear();
            listAdapter = new ListAdapter(GestActivity.this, R.layout.card_item, myDev_list);
            list.setAdapter(listAdapter);
        }
        //내 덱 초기화
        myDeck_list.clear();
        gridAdapter = new GridAdapter(GestActivity.this,R.layout.my_deck,myDeck_list,GestActivity.this);
        gridView.setAdapter(gridAdapter);
    }
}
