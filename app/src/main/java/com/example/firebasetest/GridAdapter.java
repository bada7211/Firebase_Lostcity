package com.example.firebasetest;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class GridAdapter extends ArrayAdapter implements View.OnClickListener {

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    // 생성자로부터 전달된 ListBtnClickListener  저장.
    private ListBtnClickListener listBtnClickListener;
    private ArrayList<String> numbers = new ArrayList<String>();

    Button cardButton;

    GridAdapter(Context context, int resource, ArrayList<String> list, ListBtnClickListener clickListener) {
        super(context, resource, list);
        numbers = list;
        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;

        this.listBtnClickListener = clickListener ;
    }

    public interface ListBtnClickListener {
        void onListBtnClick(int position, View v) ;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position ;
        final Context context = parent.getContext();

        // 생성자로부터 저장된 resourceId(listview_btn_item)에 해당하는 Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId/*R.layout.listview_btn_item*/, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        cardButton = (Button) convertView.findViewById(R.id.myDeckCard);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final String cardName = (String) numbers.get(pos);
        final String number = (String) numbers.get(pos);
        // 아이템 내 각 위젯에 데이터 반영
        if(!(number.contains("1")) && (number.contains("0"))) cardButton.setText("X");
        else cardButton.setText(number);
        cardButton.setBackgroundResource(getBackResource(number));
        cardButton.setTag(pos);
        cardButton.setOnClickListener(this);

        return convertView;

    }

    @Override
    public void onClick(View v) {
        // ListBtnClickListener(MainActivity)의 onListBtnClick() 함수 호출.
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick((int)v.getTag(), v) ;
        }
    }

    public int getBackResource(String card) {
        if(card.contains("R")) return R.drawable.r_back;
        else if(card.contains("G")) return R.drawable.g_back;
        else if(card.contains("W")) return R.drawable.w_back;
        else if(card.contains("B")) return R.drawable.b_back;
        else return R.drawable.y_back;
    }
}
