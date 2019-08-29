package com.example.firebasetest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
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
    TextView total_score;
    GridView gridView;
    GridAdapter gridAdapter;
    ArrayList<String> myDeck_list = new ArrayList<String>();

    ListView myList_r,myList_g,myList_w,myList_b,myList_y;
    ListView opntList_r,opntList_g,opntList_w,opntList_b,opntList_y;
    ListAdapter listAdapter;
    Button board_r,board_g,board_w,board_b,board_y,board_deck;
    String my_state = "Ready";
    String my_selCard = "";
    View preSelView;
    View curSelView;
    ListView curListView;
    ListView preListView;
    LinearLayout total_back;
    Button curBoard;
    Button preBoard;
    int deck_count;
    int round_count = 0;
    int my_score = 0;
    int opnt_score = 0;
    int my_tscore = 0;
    int opnt_tscore = 0;
    int height;
    int list_height;
    int grid_height;
    int num_padding;
    int star_padding;
    int right_padding;
    int font_size;
    Typeface star;
    Typeface normal;

    int deck_prepadding;
    Animation animation1;
    Animation animation2;

    HashMap<String, Stack<String>> board_stack = new HashMap<String, Stack<String>>();
    HashMap<String, String> dev_last_card = new HashMap<String, String>();

    MediaPlayer mp_back;
    MediaPlayer mp_card;
    MediaPlayer mp_start;
    MediaPlayer mp_room;
    MediaPlayer mp_tern;


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
        total_score = (TextView) findViewById(R.id.total_score);
        star = Typeface.createFromAsset(this.getAssets(), "seeis.ttf");
        normal = Typeface.createFromAsset(this.getAssets(), "blackjack.ttf");
        total_back = (LinearLayout) findViewById(R.id.total_back);

        setDevClickListener();
        setBoardClickListener();
        setDeckClickListener();

        height = getScreenSize(GameActivity.this).y;
        list_height = (int)(height / 4.3);
        grid_height = (int)(height / 7);
        num_padding = (int)(height/70);
        right_padding = (int)(height/45);
        star_padding = (int)(height/50);


        font_size = (int)(height/100);

        animation1 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale);
        animation2 = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.scale2);

        mp_back = MediaPlayer.create(this,R.raw.game);
        mp_back.setLooping(true);

        mp_room = MediaPlayer.create(this,R.raw.room);
        mp_room.setLooping(true);
        mp_room.start();
        mp_tern = MediaPlayer.create(this,R.raw.tern);

        mp_card = MediaPlayer.create(this,R.raw.getcard);
        mp_start = MediaPlayer.create(this,R.raw.end);

        database = FirebaseDatabase.getInstance();
        roomDb = database.getReference().child("RoomList").child(room_name).child("State");
        roomDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String game_state = dataSnapshot.getValue().toString();
                    //카드 나눠주기 시작
                    if (game_state.equals("On")) {
                        mp_room.stop();
                        mp_start.start();
                        round.setText("On");
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference("RoomList");
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
                        updateDevList();
                        updateBoardDeck();
                        round_count = 1;
                        round.setText("1ROUND");
                        total_score.setText(my_tscore +" : "+opnt_tscore);
                        stateDb.child(room_name).child("State").setValue("Start");
                        mp_back.start();
                    }
                    //1라운드 호스트턴
                    if(game_state.contains("ROUNDH")){
                        findCurList("Clear",true);
                        my_state = "SelCard";
                        total_back.setBackgroundResource(R.drawable.game_back2);
                        mp_tern.start();
                    }
                    if(game_state.contains("ROUNDG")){
                        total_back.setBackgroundResource(R.drawable.game_back);
                    }
                    if(game_state.contains("END")){
                        FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                        DatabaseReference stateDb = state_base.getReference("RoomList").child(room_name);
                        if(game_state.contains("3")) {
                            mp_start.start();
                            stateDb.child("State").setValue("Finish");
                            my_tscore += my_score;
                            opnt_tscore += opnt_score;
                            total_score.setText(my_tscore +" : "+opnt_tscore);
                            String msg;
                            if(my_tscore < opnt_tscore) msg = "[Lose] "+ my_tscore + " : " + opnt_tscore;
                            else if(my_tscore > opnt_tscore) msg = "[Win] "+ my_tscore + " : " + opnt_tscore;
                            else msg = "[Draw] "+ my_tscore + " : " + opnt_tscore;
                            Toast.makeText(GameActivity.this,msg,Toast.LENGTH_LONG).show();
                            //TODO:: 게임종료
                        }
                        else{
                            mp_start.start();
                            stateDb.child("Host").removeValue();
                            stateDb.child("Gest").removeValue();
                            stateDb.child("Game").child("Deck").removeValue();
                            stateDb.child("Board").removeValue();
                            my_tscore += my_score;
                            opnt_tscore += opnt_score;
                            resetGame();
                            Card card = new Card();
                            for(int i=0; i<8; i++) {
                                stateDb.child("Host").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                                stateDb.child("Gest").child("Card").child(card.card_deck.peek()).setValue(card.card_deck.pop());
                            }
                            for(int i=1; i<45; i++) {
                                stateDb.child("Game").child("Deck").child(""+i+"").setValue(card.card_deck.pop());
                            }
                            round_count += 1;
                            round.setText(round_count + "ROUND");
                            total_score.setText(my_tscore +" : "+opnt_tscore);
                            stateDb.child("Game").child("DeckCount").setValue(44);
                            stateDb.child("Game").child("Score").setValue("0 : 0");
                            stateDb.child("Game").child("Round").setValue(round_count);
                            if(round_count==2) stateDb.child("State").setValue(round_count+ "ROUNDG");
                            else stateDb.child("State").setValue(round_count+ "ROUNDH");
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
        mp_back.stop();
        mp_room.stop();
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference roomDb2 = database2.getReference("RoomList");
        roomDb2.child(room_name).removeValue();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mp_back.stop();
        mp_room.stop();
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference roomDb2 = database2.getReference("RoomList");
        roomDb2.child(room_name).removeValue();
        super.onBackPressed();
    }

    @Override
    public void onListBtnClick(int position, View v, int padding_b, int padding_r) {
        if(my_state.equals("SelCard")||my_state.equals("Selected")) {
            if (curSelView == null) {
                ((Button) v).setSelected(true);
                ((Button) v).setPadding(0,0,padding_r,(padding_b*3));
                deck_prepadding = padding_b;
                curSelView = v;
            } else {
                ((Button) v).setSelected(true);
                preSelView = curSelView;
                curSelView = v;
                if (curSelView != preSelView) {
                    ((Button) v).setPadding(0,0,padding_r,(padding_b*3));
                    ((Button) preSelView).setSelected(false);
                    ((Button) preSelView).setPadding(0,0,padding_r,deck_prepadding);
                    deck_prepadding = padding_b;
                }
            }
            my_selCard = myDeck_list.get(position);
            my_state = "Selected";
            mp_card.start();
            findCurList(my_selCard.substring(0,1),true);
            if(preListView!=null) preListView.setEnabled(false);
            if(preBoard!=null) preBoard.setEnabled(false);
            preListView = curListView;
            //TODO:: 리스트뷰 배경없애기, 검사
            preBoard = curBoard;
            if(findDecOrder(my_selCard)) curListView.setEnabled(true); //TODO:: 배경ㄱ
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
                    boardDb.child("Host").child("Card").child(card).setValue(card);
                    boardDb.child("Board").setValue("Rmv"+card);
                    FirebaseDatabase state_base = FirebaseDatabase.getInstance();
                    DatabaseReference stateDb = state_base.getReference().child("RoomList");
                    stateDb.child(room_name).child("State").setValue(round_count+"ROUNDG");
                    my_state = "Ready";
                }
                if(my_state.equals("Selected")) {
                    curSelView.setSelected(false);
                    setBoardEnable(true);
                    curBoard.setEnabled(false);
                    curListView.setEnabled(false);
                    FirebaseDatabase board_base = FirebaseDatabase.getInstance();
                    DatabaseReference boardDb = board_base.getReference().child("RoomList").child(room_name);
                    boardDb.child("Board").setValue("Add"+my_selCard);
                    boardDb.child("Host").child("Card").child(my_selCard).removeValue();
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
                            getdeckDb.child("Host").child("Card").child(card).setValue(card);
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
                        curSelView.setSelected(false);
                        curListView.setEnabled(false);
                        curBoard.setEnabled(false);
                        FirebaseDatabase dev_base = FirebaseDatabase.getInstance();
                        DatabaseReference devDb = dev_base.getReference().child("RoomList").child(room_name).child("Host");
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
                        else stateDb.child(room_name).child("State").setValue(round_count+"ROUNDG");
                        my_state = "Ready";
                    }
                    board_deck.startAnimation(animation2);
                    mp_card.start();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
                gridAdapter = new GridAdapter(GameActivity.this,R.layout.my_deck,myDeck_list,GameActivity.this, grid_height);
                gridView.setAdapter(gridAdapter);
                mp_card.start();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    public void updateBoard() {
        List<String> colors = Arrays.asList("R","G","W","B","Y");
        for(String color: colors) board_stack.put(color,new Stack<String>());
        FirebaseDatabase upboard_base = FirebaseDatabase.getInstance();
        DatabaseReference upboardDb = upboard_base.getReference().child("RoomList").child(room_name).child("Board");
        upboardDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String board_state = dataSnapshot.getValue().toString();
                    final String color = board_state.substring(3, 4);
                    findCurList(color, true);
                    if (board_state.contains("Add")) {
                        String card = board_state.substring(3);
                        board_stack.get(color).push(card);
                        if (!(card.contains("1")) && (card.contains("0"))) {
                            curBoard.setPadding(0,0,star_padding,star_padding);
                            curBoard.setTypeface(star);
                            curBoard.setText("A");
                        }
                        else {
                            curBoard.setPadding(0,0,right_padding,num_padding);
                            curBoard.setTypeface(normal);
                            curBoard.setText(card.substring(1));
                        }
                        curBoard.setTextSize(font_size);
                        curBoard.setBackgroundResource(findBoardBack(color,true));
                        curBoard.startAnimation(animation1);
                        mp_card.start();
                    }
                    else {
                        board_stack.get(color).pop();
                        if (board_stack.get(color).isEmpty()) {
                            curBoard.setText("");
                            curBoard.setBackgroundResource(findBoardBack(color,false));
                        }
                        else {
                            String card = board_stack.get(color).peek();
                            if (!(card.contains("1")) && (card.contains("0"))) {
                                curBoard.setPadding(0,0,star_padding,star_padding);
                                curBoard.setTypeface(star);
                                curBoard.setText("A");
                            }
                            else {
                                curBoard.setPadding(0,0,right_padding,num_padding);
                                curBoard.setTypeface(normal);
                                curBoard.setText(card.substring(1));
                            }
                            curBoard.setTextSize(font_size);
                        }
                        curBoard.startAnimation(animation2);
                        mp_card.start();
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
            dev_last_card.put(color,"0000");
            final String s_color = color;
            FirebaseDatabase h_list_base = FirebaseDatabase.getInstance();
            DatabaseReference hlistDb = h_list_base.getReference().child("RoomList").child(room_name).child("Host").child("DevCard").child(color);
            hlistDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        findCurList(dataSnapshot.getKey(),true);
                        ArrayList<String> myDev_list = new ArrayList<String>();
                        int score = 0;
                        int mult = 1;
                        String ten_card = "N";
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String card = child.getValue().toString();
                            String num = card.substring(1);
                            if(!(num.contains("1"))&&(num.contains("0"))) mult += 1;
                            else score += Integer.parseInt(num);
                            if(num.equals("10")) ten_card = card;
                            else myDev_list.add(card);
                        }
                        if(ten_card.contains("10")) myDev_list.add(ten_card);
                        dev_last_card.put(s_color,myDev_list.get(myDev_list.size()-1));
                        score = (score - 20) * mult;
                        int pre_score = Integer.parseInt(findCurScoreView(s_color,true).getText().toString());
                        findCurScoreView(s_color,true).setText(String.valueOf(score));
                        setTotalScore(score,pre_score,true);
                        listAdapter = new ListAdapter(GameActivity.this,R.layout.card_item,myDev_list,list_height, myDev_list.size());
                        curListView.setAdapter(listAdapter);
                        mp_card.start();
                        my_state = "SetCardDev";
                        setBoardEnable(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            FirebaseDatabase g_list_base = FirebaseDatabase.getInstance();
            DatabaseReference glistDb = g_list_base.getReference().child("RoomList").child(room_name).child("Gest").child("DevCard").child(color);
            glistDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        findCurList(dataSnapshot.getKey(),false);
                        ArrayList<String> opntDev_list = new ArrayList<String>();
                        int score = 0;
                        int mult = 1;
                        String ten_card = "N";
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String card = child.getValue().toString();
                            String num = card.substring(1);
                            if(!(num.contains("1"))&&(num.contains("0"))) mult += 1;
                            else score += Integer.parseInt(num);
                            if(num.equals("10")) ten_card = card;
                            else opntDev_list.add(card);
                        }
                        if(ten_card.contains("10")) opntDev_list.add(ten_card);
                        score = (score - 20) * mult;
                        int pre_score = Integer.parseInt(findCurScoreView(s_color,false).getText().toString());
                        findCurScoreView(s_color,false).setText(String.valueOf(score));
                        setTotalScore(score,pre_score,false);
                        listAdapter = new ListAdapter(GameActivity.this,R.layout.card_item,opntDev_list,list_height, opntDev_list.size());
                        curListView.setAdapter(listAdapter);
                        mp_card.start();
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
        int i = 0;
        for(Button board : boards) {
            board.setBackgroundResource(findBoardBack(colors.get(i),false));
            dev_last_card.put(colors.get(i),"0000");
            board.setText("");
            i++;
        }
        //리스트초기화
        ListView[] lists = {myList_r,myList_g,myList_w,myList_b,myList_y,
                opntList_r,opntList_g,opntList_w,opntList_b,opntList_y};
        for(ListView list : lists) {
            ArrayList<String> myDev_list = new ArrayList<String>();
            myDev_list.clear();
            listAdapter = new ListAdapter(GameActivity.this, R.layout.card_item, myDev_list, list_height, myDev_list.size());
            list.setAdapter(listAdapter);
        }
        //내 덱 초기화
        myDeck_list.clear();
        gridAdapter = new GridAdapter(GameActivity.this,R.layout.my_deck,myDeck_list,GameActivity.this, grid_height);
        gridView.setAdapter(gridAdapter);
    }

    public Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return  size;
    }

    public Boolean findDecOrder(String card) {
        String last_card = dev_last_card.get(card.substring(0,1));
        if(!(card.contains("1"))&&(card.contains("0"))){
            if(!(last_card.contains("1"))&&(last_card.contains("0"))) return true;
            else return false;
        }
        else {
            if(!(last_card.contains("1"))&&(last_card.contains("0"))) return true;
            else{
                if(Integer.parseInt(card.substring(1))>Integer.parseInt(last_card.substring(1))) return true;
                else return false;
            }
        }
    }

    public int findBoardBack(String color,Boolean flag) {
        if(flag){
            if(color.equals("R")) return R.drawable.r_board2;
            else if(color.equals("G")) return R.drawable.g_board2;
            else if(color.equals("W")) return R.drawable.w_board2;
            else if(color.equals("B")) return R.drawable.b_board2;
            else return R.drawable.y_board2;
        }
        else{
            if(color.equals("R")) return R.drawable.r_board1;
            else if(color.equals("G")) return R.drawable.g_board1;
            else if(color.equals("W")) return R.drawable.w_board1;
            else if(color.equals("B")) return R.drawable.b_board1;
            else return R.drawable.y_board1;
        }
    }

}
