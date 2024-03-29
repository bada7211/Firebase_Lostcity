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
    int layout_height;

    Button cardButton;


    GridAdapter(Context context, int resource, ArrayList<String> list, ListBtnClickListener clickListener, int height) {
        super(context, resource, list);
        numbers = list;
        layout_height = height;
        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;

        this.listBtnClickListener = clickListener ;
    }

    public interface ListBtnClickListener {
        void onListBtnClick(int position, View v, int padding_b, int padding_r) ;
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
        int num_padding = (int)(layout_height/10);
        int num_padding_b = (int)(layout_height/12);
        int star_padding = (int)(layout_height/10);
        int font_size = (int)(layout_height/18);

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        cardButton = (Button) convertView.findViewById(R.id.myDeckCard);
        Typeface star = Typeface.createFromAsset(context.getAssets(), "seeis.ttf");
        Typeface normal = Typeface.createFromAsset(context.getAssets(), "blackjack.ttf");

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final String cardName = (String) numbers.get(pos);
        final String number = (String) numbers.get(pos);
        // 아이템 내 각 위젯에 데이터 반영
        if(!(number.contains("1")) && (number.contains("0")))  {
            cardButton.setPadding(0,0,num_padding_b,star_padding);
            cardButton.setTypeface(star);
            cardButton.setText("A");
        }
        else {
            cardButton.setPadding(0,0,num_padding,num_padding_b);
            cardButton.setTypeface(normal);
            cardButton.setText(number.substring(1));
        }
        cardButton.setTextSize(font_size);
        cardButton.setBackgroundResource(getBackResource(number));
        cardButton.setTag(pos);
        cardButton.setOnClickListener(this);

        return convertView;

    }

    @Override
    public void onClick(View v) {
        // ListBtnClickListener(MainActivity)의 onListBtnClick() 함수 호출.
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick((int)v.getTag(), v, v.getPaddingBottom(), v.getPaddingRight()) ;
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
