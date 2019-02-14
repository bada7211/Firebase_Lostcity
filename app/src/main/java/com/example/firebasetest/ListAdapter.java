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

public class ListAdapter extends ArrayAdapter {

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    int back_resourceId ;

    private ArrayList<String> numbers = new ArrayList<String>();

    Button cardButton;
    String cardName;

    ListAdapter(Context context, int resource, ArrayList<String> list, int back_resource) {
        super(context, resource, list);
        numbers = list;
        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;
        this.back_resourceId = back_resource;
    }

    public interface ListBtnClickListener {
        void onListBtnClick(int position, View v, String card_name) ;
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
        cardButton = (Button) convertView.findViewById(R.id.myDevCard);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        cardName = (String) numbers.get(pos);
        final String number = (String) numbers.get(pos);
        // 아이템 내 각 위젯에 데이터 반영
        cardButton.setText(number);
        cardButton.setBackgroundResource(back_resourceId);
        cardButton.setTag(pos);

        return convertView;

    }
}