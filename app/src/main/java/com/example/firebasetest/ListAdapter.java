package com.example.firebasetest;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter {

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    int layout_height;

    private ArrayList<String> numbers = new ArrayList<String>();

    Button cardButton;
    String cardName;

    int count_list;
    int num_padding;

    ListAdapter(Context context, int resource, ArrayList<String> list, int height, int count) {
        super(context, resource, list);
        numbers = list;
        layout_height = height;
        count_list = count;
        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;
        num_padding = layout_height/12;
        if(count_list<=7) layout_height = layout_height/7;
        else layout_height = layout_height/count_list;
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

        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = layout_height;
        convertView.setLayoutParams(params);

        int star_padding = (int)(layout_height/7);
        int font_size = (int)(layout_height/5);

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        cardButton = (Button) convertView.findViewById(R.id.myDevCard);
        Typeface star = Typeface.createFromAsset(context.getAssets(), "seeis.ttf");
        Typeface normal = Typeface.createFromAsset(context.getAssets(), "blackjack.ttf");
//        cardButton.setLayoutParams(new RelativeLayout.LayoutParams(height,height));

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        cardName = (String) numbers.get(pos);
        final String number = (String) numbers.get(pos);
        // 아이템 내 각 위젯에 데이터 반영
        if(!(number.contains("1")) && (number.contains("0")))  {
            cardButton.setPadding(num_padding,0,0,0);
            cardButton.setTypeface(star);
            cardButton.setText("A");
        }
        else {
            cardButton.setPadding(num_padding,0,0,0);
            cardButton.setTypeface(normal);
            cardButton.setText(number.substring(1));
        }
        cardButton.setTextSize(font_size);
        cardButton.setBackgroundResource(getBackResource(number));
        cardButton.setTag(pos);

        return convertView;

    }

    public int getBackResource(String card) {
        if(card.contains("R")) return R.drawable.reddevcard;
        else if(card.contains("G")) return R.drawable.greendevcard;
        else if(card.contains("W")) return R.drawable.whitedevcard;
        else if(card.contains("B")) return R.drawable.bluedevcard;
        else return R.drawable.yellowdevcard;
    }
}