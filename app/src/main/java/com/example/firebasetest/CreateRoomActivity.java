package com.example.firebasetest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateRoomActivity extends Activity {

    EditText room_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_room);


        //UI 객체생성
//        txtText = (TextView)findViewById(R.id.txtText);
////
////        //데이터 가져오기
////        Intent intent = getIntent();
////        String data = intent.getStringExtra("data");
//        txtText.setText(data);
    }

    //확인 버튼 클릭
    public void createRoom_End(View v){
        //데이터 전달하기
        room_txt = (EditText) findViewById(R.id.room_name);
        String name = room_txt.getText().toString();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("RoomName", name);
        startActivity(intent);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomDb = database.getReference("RoomList");
        roomDb.child(name).child("Name").setValue(name);
        roomDb.child(name).child("State").setValue("Ready");

        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        //안드로이드 백버튼 막기
//        return;
//    }

}
